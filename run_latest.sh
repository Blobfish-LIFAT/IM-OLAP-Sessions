#!/usr/bin/env bash
git pull && mvn clean compile assembly:single && java -Xmx18G -jar target/IM-OLAP-1.0-SNAPSHOT-jar-with-dependencies.jar
