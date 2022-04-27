#!/bin/sh

if [ -z "${MYPLACE_VM_JAVA_OPTS}" ]; then
  MYPLACE_VM_JAVA_OPTS=
fi

/usr/lib/jvm/java-11/bin/java -Dloader.path=/opt/mystandard/lib/ $MYPLACE_VM_JAVA_OPTS -jar /opt/mystandard/lib/mystandard-1.1.3.jar --spring.config.location=file:/opt/mystandard/config/
