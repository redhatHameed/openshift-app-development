#!/usr/bin/env bash

set -x

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

IMAGE=jenkins-slave-maven-jnd:latest
IMAGE_NAMESPACE=jnd-jenkins
REGISTRY_HOST=docker-registry-default.apps.fra.example.opentlc.com

oc project $PROJECT

docker build -t $IMAGE .
docker tag $IMAGE $REGISTRY_HOST/$IMAGE_NAMESPACE/$IMAGE

TOKEN=`oc whoami -t`

docker login -p $TOKEN -u jusdavis-redhat.com $REGISTRY_HOST

sleep 5

docker push $REGISTRY_HOST/$IMAGE_NAMESPACE/$IMAGE
