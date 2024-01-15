@echo off
cd /d %~dp0
echo Running Zookeeper...
start 1_Start_Zookeeper.bat

echo Running Kafka...
start 2_Start_Kafka_Server.bat