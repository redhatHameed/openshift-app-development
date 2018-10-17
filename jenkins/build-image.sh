#!/usr/bin/env bash

docker login

IMAGE=jenkins
TAG=latest
REPO=jenkins

docker build -t $IMAGE:$TAG .

docker tag $IMAGE:$TAG ${REPO}/$IMAGE:$TAG

docker push ${REPO}/$IMAGE:$TAG
