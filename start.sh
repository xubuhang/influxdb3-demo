#!/bin/bash

echo "========================================"
echo "   InfluxDB v3 演示应用启动脚本"
echo "========================================"
echo

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 17或更高版本"
    exit 1
fi

echo "检查Java版本..."
java -version
echo

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请先安装Maven 3.6或更高版本"
    exit 1
fi

echo "检查Maven版本..."
mvn -version
echo

echo "开始编译项目..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi

echo
echo "编译成功！正在启动应用..."
echo
echo "应用将在 http://localhost:8080 启动"
echo "按 Ctrl+C 停止应用"
echo

echo "Starting InfluxDB v3 Demo Application..."

java --add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED \
     --add-opens=java.base/sun.nio.ch=org.apache.arrow.memory.core,ALL-UNNAMED \
     -jar target/influxdb3-demo-1.0.0.jar 