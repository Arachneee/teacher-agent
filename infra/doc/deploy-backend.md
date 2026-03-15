# Backend 배포 가이드 (EC2)

## 개요

Spring Boot 백엔드는 AWS EC2(Amazon Linux 2023, t3.micro)에 배포된다. GitHub Actions를 통해 main 브랜치 push 시 자동 배포된다.

## 인프라 구성

| 항목 | 값 |
|------|-----|
| 인스턴스 타입 | t3.micro (CPU 크레딧: standard) |
| OS | Amazon Linux 2023 |
| 리전 | ap-northeast-2 (서울) |
| 포트 | 8080 |
| JDK | Eclipse Temurin 25 |
| 앱 경로 | `/home/ec2-user/app/` |

### 보안 그룹 인바운드 규칙

| 포트 | 프로토콜 | 용도 |
|------|----------|------|
| 22 | TCP | SSH 접속 |
| 8080 | TCP | 애플리케이션 |

## Terraform으로 인프라 프로비저닝

```bash
cd infra
terraform init
terraform apply
```

생성되는 리소스:
- EC2 인스턴스 (`teacher-agent-server`)
- 보안 그룹 (`teacher-agent-sg`)
- SSH 키페어 (`teacher-agent-key`) + `teacher-agent-key.pem` 파일

## GitHub Actions 자동 배포

`backend/**` 경로 변경이 main에 push되면 자동으로 실행된다.

### 파이프라인 단계

1. **build-and-test**: Gradle 빌드 + 테스트, JAR 아티팩트 업로드
2. **deploy**: EC2에 JAR 전송 및 앱 재기동

### 필요한 GitHub Secrets

| Secret | 설명 |
|--------|------|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_SSH_KEY` | SSH 프라이빗 키 (PEM 파일 내용) |
| `OPENAI_API_KEY` | OpenAI API 키 |
| `INITIAL_TEACHER_PASSWORD` | 초기 선생님 계정 비밀번호 |

### 환경변수

배포 시 아래 환경변수가 주입된다:

```
SPRING_PROFILES_ACTIVE=prod
INITIAL_TEACHER_PASSWORD=<secret>
```

## 수동 배포

### EC2 초기 설정 (최초 1회)

```bash
scp -i infra/teacher-agent-key.pem \
  backend/scripts/setup-ec2.sh \
  ec2-user@<EC2_HOST>:~/setup-ec2.sh

ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST> \
  'chmod +x ~/setup-ec2.sh && ~/setup-ec2.sh'
```

설치 내용: Eclipse Temurin JDK 25, 앱 디렉토리(`/home/ec2-user/app/`) 생성

### JAR 빌드 및 전송

```bash
cd backend
./gradlew build

scp -i infra/teacher-agent-key.pem \
  build/libs/teacher-agent-backend-*-SNAPSHOT.jar \
  ec2-user@<EC2_HOST>:/home/ec2-user/app/teacher-agent-backend.jar
```

### 앱 실행

```bash
scp -i infra/teacher-agent-key.pem \
  backend/scripts/deploy.sh \
  ec2-user@<EC2_HOST>:/home/ec2-user/app/deploy.sh

ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST> \
  'chmod +x /home/ec2-user/app/deploy.sh && SPRING_PROFILES_ACTIVE=prod INITIAL_TEACHER_PASSWORD=<password> /home/ec2-user/app/deploy.sh'
```

## 배포 스크립트 동작

`backend/scripts/deploy.sh` 실행 시:

1. Java 설치 여부 확인 (없으면 자동 설치)
2. `cap_net_bind_service` capability를 Java 바이너리에 부여 (8080 포트 바인딩 허용)
3. `libjli.so` 경로를 ldconfig에 등록 (setcap 적용 후 LD_LIBRARY_PATH 무시 문제 해결)
4. 기존 프로세스 종료 (`pkill -f "java.*teacher-agent"`)
5. `nohup java -jar teacher-agent-backend.jar` 로 재기동

로그: `/home/ec2-user/app/app.log`

## SSH 접속

```bash
ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST>
```

## 로그 확인

```bash
ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST> \
  'tail -f /home/ec2-user/app/app.log'
```
