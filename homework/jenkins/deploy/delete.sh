#!/usr/bin/env bash

. ../../env.sh

oc project ${CICD_PROJECT}

oc delete all -l app=jenkins
oc delete pvc jenkins-data
oc delete serviceaccount jenkins