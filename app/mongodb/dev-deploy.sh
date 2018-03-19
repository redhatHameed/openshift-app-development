#!/usr/bin/env bash

. ../../env.sh

oc login https://${IP}:8443 -u $USER

oc project ${DEV_PROJECT}

oc new-app -f mongo-statefulset-template.yml \
    -p REPLICAS=1


