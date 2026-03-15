#!/bin/bash

echo "=== Teacher Agent EC2 Setup Script ==="
echo "Run this script on your EC2 Ubuntu instance"
echo ""

sudo apt-get update
sudo apt-get install -y wget apt-transport-https gpg

wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

sudo apt-get update
sudo apt-get install -y temurin-25-jdk

mkdir -p /home/ubuntu/app

echo ""
echo "=== Setup Complete ==="
echo "JDK version:"
java -version
echo ""
echo "App directory created at: /home/ubuntu/app"
