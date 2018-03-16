#!/bin/bash

#set -e
#
#if [ "${1:0:1}" != '-' ]; then
#  exec "$@"
#fi
#
##chown -R sonarqube:sonarqube $SONARQUBE_HOME

echo SONAR_VERSION = $SONAR_VERSION
echo SONARQUBE_JDBC_USERNAME = $SONARQUBE_JDBC_USERNAME
echo SONARQUBE_JDBC_PASSWORD = $SONARQUBE_JDBC_PASSWORD
echo SONARQUBE_JDBC_URL = $SONARQUBE_JDBC_URL

exec java -jar lib/sonar-application-$SONAR_VERSION.jar \
  -Dsonar.log.console=true \
  -Dsonar.jdbc.username="$SONARQUBE_JDBC_USERNAME" \
  -Dsonar.jdbc.password="$SONARQUBE_JDBC_PASSWORD" \
  -Dsonar.jdbc.url="$SONARQUBE_JDBC_URL" \
  -Dsonar.web.javaAdditionalOpts="$SONARQUBE_WEB_JVM_OPTS -Djava.security.egd=file:/dev/./urandom" \
  "$@"