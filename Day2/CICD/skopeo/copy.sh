#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

SRC_USER=jusdavis-redhat.com
SRC_PASSWORD=$(oc whoami -t)
SRC_REGISTRY_HOST=docker-registry-default.apps.fra.example.opentlc.com

DEST_USER=admin
DEST_PASSWORD=admin123
DEST_REGISTRY_HOST=registry-jnd-nexus.apps.fra.example.opentlc.com



skopeo \
    --insecure-policy \
    copy \
    --src-creds=${SRC_USER}:${SRC_PASSWORD} \
    --dest-creds=${DEST_USER}:${DEST_PASSWORD} \
    --src-tls-verify=false \
    --dest-tls-verify=false \
    docker://${SRC_REGISTRY_HOST}/jnd-jenkins/jenkins-slave-maven-jnd:latest \
    docker://${DEST_REGISTRY_HOST}/jnd-jenkins/jenkins-slave-maven-jnd:latest



