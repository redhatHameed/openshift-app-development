#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-jenkins

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done

oc new-app jenkins-persistent \
    -p VOLUME_CAPACITY=4Gi \
    -p MEMORY_LIMIT=2Gi \
    -p JENKINS_IMAGE_STREAM_TAG=jenkins:v3.7



