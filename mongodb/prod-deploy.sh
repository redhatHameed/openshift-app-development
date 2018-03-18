#!/usr/bin/env bash

. ../env.sh

oc login https://${IP}:8443 -u $USER

oc project ${PROD_PROJECT}

oc new-app -f mongo-statefulset-template.yml \
    -p REPLICAS=3


