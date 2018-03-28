#!/usr/bin/env bash

. ../env.sh

oc project $CICD_PROJECT

oc new-app  -f gogs-persistent-template.yml \
    -p DOMAIN=${DOMAIN} \
    -p APPLICATION_NAME=gogs \
    -p GOGS_VOLUME_CAPACITY=1Gi \
    -p DATABASE_USER=${DATABASE_USER} \
    -p DATABASE_PASSWORD=${DATABASE_PASSWORD} \
    -p DATABASE_NAME=${DATABASE_NAME} \
    -p GOGS_VERSION="latest" \
    -p INSTALL_LOCK=true \
    -p SKIP_TLS_VERIFY=true \
    -p HOSTNAME=gogs-cicd.apps.ocp.datr.eu
