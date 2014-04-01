#!/bin/bash
java -Dcom.sun.management.jmxremote.port=9998 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Djava.util.logging.config.file=logging.properties \
  -jar target/filewatcher.jar $@
