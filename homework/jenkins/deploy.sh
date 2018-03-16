#!/usr/bin/env bash

. ../env.sh

oc login https://${IP}:8443 -u $USER

oc delete project jenkins
oc new-project jenkins 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project jenkins 2> /dev/null
done

#oc delete all --all
#oc delete pvc jenkins-data
#oc delete serviceaccount jenkins

oc new-app -f jenkins-persistent-template.yml \
    -p DOMAIN=${DOMAIN} \
    -p APPLICATION_NAME=jenkins \
    -p SOURCE_REPOSITORY_URL=https://github.com/justindav1s/ocp-appdev.git \
    -p SOURCE_REPOSITORY_URL=master \
    -p DOCKERFILE_PATH="homework/jenkins" \
    -p MEMORY_LIMIT=2Gi \
    -p VOLUME_REQUEST=5Gi
