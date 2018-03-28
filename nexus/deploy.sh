#!/usr/bin/env bash

. ../env.sh

oc project $CICD_PROJECT

oc delete all -l app=nexus
oc delete pvc nexus-pv

oc new-app -f nexus-persistent-template.yaml


