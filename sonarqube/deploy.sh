#!/usr/bin/env bash

. ../env.sh

oc project $CICD_PROJECT

oc new-app -f sonarqube-persistent-template.yml \
    -p DOMAIN=${DOMAIN} \
    -p APPLICATION_NAME=sonarqube \
    -p SOURCE_REPOSITORY_URL=https://github.com/justindav1s/ocp-appdev.git \
    -p SOURCE_REPOSITORY_URL=master \
    -p DOCKERFILE_PATH="homework/sonarqube" \
    -p SONARQUBE_JDBC_USERNAME=${DATABASE_USER} \
    -p SONARQUBE_JDBC_PASSWORD=${DATABASE_PASSWORD} \
    -p SONARQUBE_JDBC_URL=${DATABASE_URL}
