#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-rocket

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done


oc create -f headless-service-template.yaml

oc create -f mongo-service-template.yaml

oc create -f mongo-statefulset.yaml


