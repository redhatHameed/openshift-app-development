#!/usr/bin/env bash

. ../env.sh

oc delete all -l app=jenkins
oc delete pvc jenkins-data
oc delete serviceaccount jenkins

oc new-app -f jenkins-persistent-template.yml \
    -p DOMAIN=${DOMAIN} \
    -p APPLICATION_NAME=jenkins \
    -p JENKINS_VERSION=2 \
    -p SOURCE_REPOSITORY_URL=https://github.com/justindav1s/ocp-appdev.git \
    -p SOURCE_REPOSITORY_URL=master \
    -p DOCKERFILE_PATH="jenkins" \
    -p MEMORY_LIMIT=2Gi \
    -p VOLUME_REQUEST=5Gi
