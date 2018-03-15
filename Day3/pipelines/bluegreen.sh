#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

oc project jnd-tasks-prod

oc patch route/tasks -p '{"spec":{"to":{"name":"tasks-green"}}}' -n jnd-tasks-prod


