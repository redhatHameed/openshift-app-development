#!/usr/bin/env bash

oc new-app -f headless-service-template.yaml

oc new-app -f mongo-service-template.yaml

oc new-app -f mongo-statefulset.yaml


