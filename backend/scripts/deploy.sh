#!/bin/bash

APP_NAME="teacher-agent"
APP_DIR="/home/ec2-user/app"
JAR_FILE="$APP_DIR/teacher-agent-backend.jar"
LOG_FILE="$APP_DIR/app.log"

install_java() {
    echo "Java not found. Installing JDK 25..."
    sudo dnf install -y wget tar

    wget -O /tmp/temurin-25.tar.gz \
      "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk"
    sudo mkdir -p /usr/lib/jvm
    sudo tar -xzf /tmp/temurin-25.tar.gz -C /usr/lib/jvm
    JAVA_DIR=$(ls /usr/lib/jvm | grep -i jdk-25)
    sudo ln -sf /usr/lib/jvm/$JAVA_DIR/bin/java /usr/bin/java
    sudo ln -sf /usr/lib/jvm/$JAVA_DIR/bin/keytool /usr/bin/keytool
    rm -f /tmp/temurin-25.tar.gz

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
        sudo dnf install -y libcap
        sudo setcap 'cap_net_bind_service=+ep' "$JAVA_REAL_PATH"
    fi

    # setcap 적용 후 LD_LIBRARY_PATH가 무시되므로 libjli.so 경로를 ldconfig에 등록
    JAVA_LIB_DIR=$(find /usr/lib/jvm -name "libjli.so" -exec dirname {} \; | head -1)
    if [ -n "$JAVA_LIB_DIR" ]; then
        echo "$JAVA_LIB_DIR" | sudo tee /etc/ld.so.conf.d/jdk.conf
        sudo ldconfig
        echo "Registered JDK library path: $JAVA_LIB_DIR"
    fi
}

setup_java_port_binding

echo "Stopping existing application..."
pkill -f "java.*teacher-agent" || true
sleep 2

echo "Starting application..."
cd $APP_DIR
nohup env \
  SPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE" \
  SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
  SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
  OPENAI_API_KEY="$OPENAI_API_KEY" \
  INITIAL_TEACHER_PASSWORD="$INITIAL_TEACHER_PASSWORD" \
  java \
    -Xms128m -Xmx384m \
    -XX:+UseG1GC \
    -XX:MaxMetaspaceSize=128m \
    -Djava.security.egd=file:/dev/./urandom \
    -jar $JAR_FILE > $LOG_FILE 2>&1 &

sleep 3

if pgrep -f "java.*teacher-agent" > /dev/null; then
    echo "Application started successfully!"
    echo "PID: $(pgrep -f 'java.*teacher-agent')"
else
    echo "Failed to start application. Check logs at $LOG_FILE"
    exit 1
fi

echo "Starting warmup in background..."
chmod +x $APP_DIR/warmup.sh
WARMUP_TEACHER_USER_ID="$WARMUP_TEACHER_USER_ID" \
WARMUP_TEACHER_PASSWORD="$WARMUP_TEACHER_PASSWORD" \
  nohup bash $APP_DIR/warmup.sh >> $APP_DIR/warmup.log 2>&1 &
