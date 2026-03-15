#!/bin/bash

APP_NAME="teacher-agent"
APP_DIR="/home/ubuntu/app"
JAR_FILE="$APP_DIR/teacher-agent-backend.jar"
LOG_FILE="$APP_DIR/app.log"

install_java() {
    echo "Java not found. Installing JDK 25..."
    sudo apt-get install -y wget apt-transport-https gpg
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
    sudo apt-get update
    sudo apt-get install -y temurin-25-jdk

    if ! command -v java &> /dev/null; then
        echo "Failed to install Java!"
        exit 1
    fi
    echo "Java installed successfully!"
}

setup_java_port_binding() {
    echo "Setting up Java to bind privileged ports..."

    JAVA_PATH=$(which java)
    if [ -z "$JAVA_PATH" ]; then
        install_java
        JAVA_PATH=$(which java)
    fi

    JAVA_REAL_PATH=$(readlink -f "$JAVA_PATH")
    echo "Java binary: $JAVA_REAL_PATH"

    sudo setcap 'cap_net_bind_service=+ep' "$JAVA_REAL_PATH"
    if [ $? -eq 0 ]; then
        echo "Successfully granted port binding capability to Java."
    else
        echo "Failed to set capability. Trying alternative method..."
        sudo apt-get update
        sudo apt-get install -y libcap2-bin
        sudo setcap 'cap_net_bind_service=+ep' "$JAVA_REAL_PATH"
    fi
}

setup_java_port_binding

echo "Stopping existing application..."
pkill -f "java.*teacher-agent" || true
sleep 2

echo "Starting application..."
cd $APP_DIR
nohup java -jar $JAR_FILE > $LOG_FILE 2>&1 &

sleep 3

if pgrep -f "java.*teacher-agent" > /dev/null; then
    echo "Application started successfully!"
    echo "PID: $(pgrep -f 'java.*teacher-agent')"
else
    echo "Failed to start application. Check logs at $LOG_FILE"
    exit 1
fi
