# Backend Deployment Guide (EC2)

## Purpose
This guide outlines the process for deploying the Spring Boot backend application to an AWS EC2 instance, covering infrastructure setup, automated CI/CD pipelines, and manual deployment procedures.

## Overview
The Spring Boot backend is deployed on AWS EC2 (Amazon Linux 2023, t3.micro instance). Automated deployment is triggered by pushes to the main branch via GitHub Actions.

## Infrastructure Configuration

### EC2 Instance Details
| Item            | Value                               | Notes                       |
| :-------------- | :---------------------------------- | :-------------------------- |
| Instance Type   | t3.micro                            | Standard CPU credits        |
| OS              | Amazon Linux 2023                   |                             |
| Region          | ap-northeast-2 (Seoul)              |                             |
| Application Port| 8080                                | Used by the Spring Boot app |
| JDK Version     | Eclipse Temurin 25                  |                             |
| Application Path| `/home/ec2-user/app/`               | Where the JAR file is placed|

### Security Group Inbound Rules
| Port | Protocol | Purpose       |
| :--- | :------- | :------------ |
| 22   | TCP      | SSH Access    |
| 8080 | TCP      | Application Traffic |

## Infrastructure Provisioning with Terraform

To provision the necessary AWS infrastructure (EC2 instance, security group, SSH key pair), navigate to the `infra/` directory and run:

```bash
cd infra
terraform init
terraform apply
```

This will create:
-   EC2 instance named `teacher-agent-server`.
-   Security group named `teacher-agent-sg`.
-   SSH key pair named `teacher-agent-key`, along with a `teacher-agent-key.pem` file.

## GitHub Actions Automated Deployment

Automated deployment is triggered for changes pushed to the main branch affecting files under the `backend/**` path.

### Pipeline Stages
1.  **`build-and-test`**: Executes Gradle build and tests, then uploads the JAR artifact.
2.  **`deploy`**: Transfers the JAR to EC2 and restarts the application.

### Required GitHub Secrets
Ensure the following secrets are configured in your GitHub repository settings:
-   `EC2_HOST`: Public IP address of the EC2 instance.
-   `EC2_SSH_KEY`: Content of the SSH private key (`.pem` file).
-   `OPENAI_API_KEY`: OpenAI API key for backend services.
-   `INITIAL_TEACHER_PASSWORD`: Password for the initial teacher account.

### Environment Variables for Deployment
The following environment variables are injected during deployment:
-   `SPRING_PROFILES_ACTIVE=prod`
-   `INITIAL_TEACHER_PASSWORD=<secret>`

## Manual Deployment Steps

### EC2 Initial Setup (One-time)
1.  Copy the setup script to EC2:
    ```bash
    scp -i infra/teacher-agent-key.pem 
      backend/scripts/setup-ec2.sh 
      ec2-user@<EC2_HOST>:~/setup-ec2.sh
    ```
2.  Execute the script on EC2:
    ```bash
    ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST> 
      'chmod +x ~/setup-ec2.sh && ~/setup-ec2.sh'
    ```
    This installs Eclipse Temurin JDK 25 and creates the application directory (`/home/ec2-user/app/`).

### Build and Transfer JAR
1.  Build the JAR locally:
    ```bash
    cd backend
    ./gradlew build
    ```
2.  Copy the JAR file to EC2:
    ```bash
    scp -i infra/teacher-agent-key.pem 
      build/libs/teacher-agent-backend-*-SNAPSHOT.jar 
      ec2-user@<EC2_HOST>:/home/ec2-user/app/teacher-agent-backend.jar
    ```

### Run Application
1.  Copy the deployment script to EC2:
    ```bash
    scp -i infra/teacher-agent-key.pem 
      backend/scripts/deploy.sh 
      ec2-user@<EC2_HOST>:/home/ec2-user/app/deploy.sh
    ```
2.  Execute the deployment script on EC2:
    ```bash
    ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST> 
      'chmod +x /home/ec2-user/app/deploy.sh && SPRING_PROFILES_ACTIVE=prod INITIAL_TEACHER_PASSWORD=<password> /home/ec2-user/app/deploy.sh'
    ```

## Deployment Script Execution Details

The `backend/scripts/deploy.sh` script performs the following actions:
1.  Checks for Java installation and installs if necessary.
2.  Grants `cap_net_bind_service` capability to the Java binary to allow binding to port 8080.
3.  Registers `libjli.so` path in `ldconfig` to resolve issues with `setcap` and `LD_LIBRARY_PATH`.
4.  Stops any existing running application instance (`pkill -f "java.*teacher-agent"`).
5.  Restarts the application using `nohup java -jar teacher-agent-backend.jar`.

Application logs are stored in `/home/ec2-user/app/app.log`.

## SSH Access

To access the EC2 instance:
```bash
ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST>
```

## Log Monitoring

To view application logs in real-time:
```bash
ssh -i infra/teacher-agent-key.pem ec2-user@<EC2_HOST> 
  'tail -f /home/ec2-user/app/app.log'
