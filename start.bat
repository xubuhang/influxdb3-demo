@echo off
echo Starting InfluxDB v3 Demo Application...

java --add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED ^
     --add-opens=java.base/sun.nio.ch=org.apache.arrow.memory.core,ALL-UNNAMED ^
     -jar target/influxdb3-demo-1.0.0.jar

pause 