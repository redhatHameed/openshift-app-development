#!/usr/bin/env bash

export IP=ocp.datr.eu
export USER=justin
export ORG=mitzicom
export CICD_PROJECT=cicd
export TEST_PROJECT=${ORG}-test
export PROD_PROJECT=${ORG}-prod

export DOMAIN=${CICD_PROJECT}
export DATABASE_NAME=${DOMAIN}
export DATABASE_USER=${DATABASE_NAME}
export DATABASE_PASSWORD=${DATABASE_NAME}
export DATABASE_ADMIN_PASSWORD=${DATABASE_NAME}
export DATABASE_URL="jdbc:postgresql://postgres-"${DATABASE_NAME}"/"${DATABASE_NAME}

