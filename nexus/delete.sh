#!/usr/bin/env bash

. ../env.sh

oc project ${CICD_PROJECT}

oc delete all -l app=nexus