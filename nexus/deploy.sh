#!/usr/bin/env bash

. ../mitzicom/env.sh

oc delete all -l app=nexus
oc delete pvc nexus-pv

oc new-app -f nexus-persistent-template.yaml


