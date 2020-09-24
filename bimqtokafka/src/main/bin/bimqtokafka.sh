#!/usr/bin/env bash

JAR_PATH=/data/fnbi/rabbitmq/${project.artifactId}-${project.version}/${project.artifactId}-${project.version}.jar
APP_NAME=${project.artifactId}
LOG_HOME=/data/fnbi/logs/bimqtokafka

function useAge(){
    echo "Usage: sh bimqtokafka.sh <pay|dlvr> <topic> <ehcache-web-port>"
    exit 1
}

function checkType(){
    case "${type}" in
        "pay")
        echo "input type is ${type}"
        ;;
        "dlvr")
        echo "input type is ${type}"
        ;;
        *)
        useAge
        ;;
    esac
}

function isExist(){
    checkType
    # pid=`ps -ef | grep ${APP_NAME} | grep ${type} | grep -v grep| awk '{print $2}'`
    pid=`jps -m | grep ${APP_NAME} | grep ${type} | grep ${topic} | awk '{print $1}'`
    if [[ -n "${pid}" ]]; then
        return 1
    else
        return 0
    fi
}

function start(){
    isExist
    if [[ $? -eq "1" ]]; then
        echo "${APP_NAME} ${type} ${topic}is already running.pid=${pid}."
    else
        nohup java -jar -Dapp.env=${type} -Dserver.port=${port} -Dapp.log.path=${LOG_HOME} -Dapp.log.file=${topic} ${JAR_PATH} ${type} ${topic}> /dev/null 2>&1 &
        echo "${APP_NAME} ${type} ${topic} start success."
    fi
}

if [[ $# -lt 3 ]]; then
    useAge
else
    type=$1
    topic=$2
    port=$3
    start
fi
