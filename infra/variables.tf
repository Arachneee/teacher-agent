variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "instance_type" {
  description = "EC2 인스턴스 타입 (프리티어: t2.micro)"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "EC2 SSH 키페어 이름"
  type        = string
  default     = "teacher-agent-key"
}

variable "project_name" {
  description = "프로젝트 이름 (리소스 태깅용)"
  type        = string
  default     = "teacher-agent"
}
