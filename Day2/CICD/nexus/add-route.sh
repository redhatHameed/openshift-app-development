#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-nexus

oc project ${PROJECT}

oc delete service docker-registry
oc delete route registry

oc new-app -f registry-route.yml


