#!/usr/bin/env bash

export IP=ocp.datr.eu
export USER=justin

export CICD_PROJECT=cicd
export DEV_PROJECT=${ORG}-dev
export PROD_PROJECT=${ORG}-prod

export DOMAIN=${CICD_PROJECT}
export DATABASE_NAME=${DOMAIN}
export DATABASE_USER=${DATABASE_NAME}
export DATABASE_PASSWORD=${DATABASE_NAME}
export DATABASE_ADMIN_PASSWORD=${DATABASE_NAME}
export DATABASE_URL="jdbc:postgresql://postgresql/"${DATABASE_NAME}

