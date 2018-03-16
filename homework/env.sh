#!/usr/bin/env bash

export IP=ocp.datr.eu
export USER=justin
export CICD_PROJECT=cicd

export DOMAIN=${CICD_PROJECT}
export DATABASE_NAME=${DOMAIN}
export DATABASE_USER=${DATABASE_NAME}
export DATABASE_PASSWORD=${DATABASE_NAME}
export DATABASE_ADMIN_PASSWORD=${DATABASE_NAME}
export DATABASE_URL="jdbc:postgresql://postgres-"${DATABASE_NAME}"/"${DATABASE_NAME}


oc login https://${IP}:8443 -u $USER
