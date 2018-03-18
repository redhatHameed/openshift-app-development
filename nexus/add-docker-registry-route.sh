#!/usr/bin/env bash

. ../env.sh

oc project cicd

oc delete service docker-registry
oc delete route registry

oc new-app -f registry-route.yml


