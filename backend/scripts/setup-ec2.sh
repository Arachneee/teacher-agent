#!/bin/bash

echo "=== Teacher Agent EC2 Setup Script ==="
echo "Run this script on your EC2 Amazon Linux 2023 instance"
echo ""

sudo dnf update -y
sudo dnf install -y wget tar

wget -O /tmp/temurin-25.tar.gz \
  "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk"
sudo mkdir -p /usr/lib/jvm
sudo tar -xzf /tmp/temurin-25.tar.gz -C /usr/lib/jvm
JAVA_DIR=$(ls /usr/lib/jvm | grep -i jdk-25)
sudo ln -sf /usr/lib/jvm/$JAVA_DIR/bin/java /usr/bin/java
sudo ln -sf /usr/lib/jvm/$JAVA_DIR/bin/keytool /usr/bin/keytool
rm -f /tmp/temurin-25.tar.gz

mkdir -p /home/ec2-user/app

bash ~/setup-swap.sh 2G

echo ""
echo "=== Setup Complete ==="
echo "JDK version:"
java -version
echo ""
echo "App directory created at: /home/ec2-user/app"
