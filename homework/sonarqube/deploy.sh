#!/usr/bin/env bash

. ../env.sh

oc new-app -f sonarqube-persistent-template.yml \
    -p POSTGRESQL_USERNAME=${DATABASE_USER} \
    -p POSTGRESQL_PASSWORD=${DATABASE_PASSWORD} \
    -p POSTGRESQL_JDBC_URL=${DATABASE_URL}