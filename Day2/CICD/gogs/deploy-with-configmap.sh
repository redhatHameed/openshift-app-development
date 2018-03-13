#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-gogs

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done

DOMAIN="gogs"
DATABASE_NAME=${DOMAIN}
DATABASE_USER=${DATABASE_NAME}
DATABASE_PASSWORD=${DATABASE_NAME}
DATABASE_ADMIN_PASSWORD=${DATABASE_NAME}
DATABASE_URL="jdbc:postgresql://postgres-"${DATABASE_NAME}"/"${DATABASE_NAME}

oc new-app -f postgres-persistent-template.yaml \
    -p DOMAIN=${DOMAIN} \
    -p APPLICATION_NAME=postgres \
    -p DB_VOLUME_CAPACITY=1Gi \
    -p DATABASE_USER=${DATABASE_USER} \
    -p DATABASE_PASSWORD=${DATABASE_PASSWORD} \
    -p DATABASE_NAME=${DATABASE_NAME} \
    -p DATABASE_ADMIN_PASSWORD=${DATABASE_ADMIN_PASSWORD} \
    -p DATABASE_MAX_CONNECTIONS=100 \
    -p DATABASE_SHARED_BUFFERS=12MB

oc create configmap gogs-appini.configmap \
            --from-file=app.ini

oc new-app -f gogs-configmap-template.yaml



