#!/usr/bin/env bash

. ../../env.sh

oc login https://${IP}:8443 -u $USER

oc project ${DEV_PROJECT}
APP=mlbparks

oc delete all -l app=${APP}
oc delete is,bc ${APP}
oc delete template ${APP}-dev-dc

echo Setting up ${APP}
oc new-build --binary=true --labels=app=${APP} --name=${APP} jboss-eap70-openshift:1.6 -n ${DEV_PROJECT}
#oc new-app ${DEV_PROJECT}/${APP}:0.0-0 --name=${APP} --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
#oc set triggers dc/${APP} --remove-all -n ${DEV_PROJECT}
oc new-app -f ${APP}-dev-dc.yaml --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
oc expose dc ${APP} --port 8080 -n ${DEV_PROJECT}
oc expose svc ${APP} -n ${DEV_PROJECT}


