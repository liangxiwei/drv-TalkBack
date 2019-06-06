#!/bin/sh

if [ -d ../proto_gen ]; then
    echo "proto dir exists"
else
    echo "create proto dir"
    mkdir ../proto_gen/
fi

protoc --java_out=/home/qhb/as_workspace/pocdemo/app/src/main/java/ *.proto
if [ "$?" = "0" ]; then
    echo "生成proto 成功"
else
    echo "生成失败！！！！"
fi