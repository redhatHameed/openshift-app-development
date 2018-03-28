#!/usr/bin/env bash

. ../env.sh

oc project $CICD_PROJECT

oc delete service docker-registry
oc delete route registry

oc new-app -f registry-route.yml


