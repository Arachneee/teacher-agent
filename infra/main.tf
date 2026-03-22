terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = var.region
}

# ========================================
# SSH Key Pair
# ========================================

resource "tls_private_key" "ssh" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "this" {
  key_name   = var.key_name
  public_key = tls_private_key.ssh.public_key_openssh
}

resource "local_file" "private_key" {
  content         = tls_private_key.ssh.private_key_pem
  filename        = "${path.module}/${var.key_name}.pem"
  file_permission = "0400"
}

# ========================================
# Default VPC (프리티어 — 추가 비용 없음)
# ========================================

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# ========================================
# Security Group
# ========================================

resource "aws_security_group" "this" {
  name        = "${var.project_name}-sg"
  description = "Allow SSH and application traffic"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Application"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-sg"
  }
}

# ========================================
# RDS Security Group (EC2에서만 MySQL 접근 허용)
# ========================================

resource "aws_security_group" "rds" {
  name        = "${var.project_name}-rds-sg"
  description = "Allow MySQL only from EC2 security group"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "MySQL from EC2"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.this.id]
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-rds-sg"
  }
}

# ========================================
# RDS Subnet Group (Default VPC 서브넷 사용)
# ========================================

resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-rds-subnet-group"
  subnet_ids = data.aws_subnets.default.ids

  tags = {
    Name = "${var.project_name}-rds-subnet-group"
  }
}

# ========================================
# RDS Instance (프리티어 — db.t3.micro, 20GB gp2)
# ========================================

resource "aws_db_instance" "this" {
  identifier = "${var.project_name}-db"

  # 프리티어 조건: db.t3.micro + MySQL 8.0 + 20GB gp2
  instance_class    = "db.t3.micro"
  engine            = "mysql"
  engine_version    = "8.0"
  allocated_storage = 20
  storage_type      = "gp2" # gp3는 프리티어 대상 아님

  # Multi-AZ 비활성화 — 활성화 시 즉시 과금
  multi_az = false

  db_name  = var.rds_db_name
  username = var.rds_username
  password = var.rds_password

  # 퍼블릭 접근 차단 — EC2 보안 그룹을 통해서만 접근
  publicly_accessible    = false
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.this.name

  # 자동 백업 비활성화 — 백업 스토리지도 무료 한도 초과 시 과금 가능
  backup_retention_period = 0

  # 최종 스냅샷 생략 — destroy 시 스냅샷 스토리지 비용 방지
  skip_final_snapshot = true
  deletion_protection = false

  # 마이너 버전 자동 업그레이드 (보안 패치)
  auto_minor_version_upgrade = true

  tags = {
    Name = "${var.project_name}-db"
  }
}

# ========================================
# EC2 Instance (프리티어)
# ========================================

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "this" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.this.key_name
  vpc_security_group_ids = [aws_security_group.this.id]
  subnet_id              = data.aws_subnets.default.ids[0]

  associate_public_ip_address = true

  # standard: 크레딧 소진 시 버스트 중단 (unlimited은 버스트 초과분 과금)
  credit_specification {
    cpu_credits = "standard"
  }

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
    encrypted   = true
  }

  tags = {
    Name = "${var.project_name}-server"
  }
}

# ========================================
# Elastic IP (고정 퍼블릭 IP)
# ========================================

resource "aws_eip" "this" {
  domain = "vpc"

  tags = {
    Name = "${var.project_name}-eip"
  }
}

resource "aws_eip_association" "this" {
  instance_id   = aws_instance.this.id
  allocation_id = aws_eip.this.id
}
