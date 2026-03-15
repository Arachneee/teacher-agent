output "instance_id" {
  description = "EC2 인스턴스 ID"
  value       = aws_instance.this.id
}

output "public_ip" {
  description = "EC2 퍼블릭 IP"
  value       = aws_instance.this.public_ip
}

output "ssh_command" {
  description = "SSH 접속 명령어"
  value       = "ssh -i ${path.module}/${var.key_name}.pem ec2-user@${aws_instance.this.public_ip}"
}

output "application_url" {
  description = "애플리케이션 URL"
  value       = "http://${aws_instance.this.public_ip}:8080"
}
