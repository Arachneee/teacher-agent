#!/bin/bash

echo "=== EvoShot EC2 Setup Script ==="
echo "Run this script on your EC2 Ubuntu instance"
echo ""

sudo apt update
sudo apt install -y openjdk-21-jre-headless

mkdir -p /home/ubuntu/app

echo ""
echo "=== Setup Complete ==="
echo "JDK version:"
java -version
echo ""
echo "App directory created at: /home/ubuntu/app"

