#!/usr/bin/env bash

. ../env.sh

oc new-app -f sonarqube-persistent-template.yml \
    -p DOMAIN=${DOMAIN} \
    -p APPLICATION_NAME=sonarqube \
    -p SONARQUBE_JDBC_USERNAME=${DATABASE_USER} \
    -p SONARQUBE_JDBC_PASSWORD=${DATABASE_PASSWORD} \
    -p SONARQUBE_JDBC_URL=${DATABASE_URL}

sleep 5

oc logs -f bc/sonarqube-docker-build