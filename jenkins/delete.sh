#!/usr/bin/env bash

. ../mitzicom/env.sh

oc project ${CICD_PROJECT}

oc delete all -l app=jenkins
oc delete pvc jenkins-data
oc delete serviceaccount jenkins