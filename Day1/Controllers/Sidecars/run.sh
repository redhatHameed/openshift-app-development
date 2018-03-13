#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-deployments
DESC="JND Deployments"

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done

oc new-project ${PROJECT} --display-name=${DESC}

oc new-app --name='blue' --labels=name="blue" php~https://github.com/wkulhanek/cotd.git --env=SELECTOR=pets

oc expose service blue --name=bluegreen --port=8080

oc new-app --name='green' --labels=name="green" php~https://github.com/wkulhanek/cotd.git --env=SELECTOR=cities


