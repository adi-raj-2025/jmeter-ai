#!/bin/bash

mvn clean package

if [ -f C:\\apache-jmeter-5.6.3\\lib\\ext\\jmeter-agent-2.0.8-jar-with-dependencies.jar ]; then
    rm C:\\apache-jmeter-5.6.3\\lib\\ext\\jmeter-agent-2.0.8-jar-with-dependencies.jar
fi

cp target/jmeter-agent-2.0.8-jar-with-dependencies.jar C:\\apache-jmeter-5.6.3\\lib\\ext

