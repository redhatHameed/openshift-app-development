#!/usr/bin/env bash

. ../mitzicom/env.sh

oc project ${CICD_PROJECT}

oc delete all -l app=nexus