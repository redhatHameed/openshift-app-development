#!/usr/bin/env bash

. ../../env.sh

oc login https://${IP}:8443 -u $USER

oc project ${DEV_PROJECT}
APP=mlbparks

oc delete all -l app=${APP} -n ${DEV_PROJECT}
oc delete is,bc,configmap ${APP} -n ${DEV_PROJECT}
oc delete template ${APP}-dev-dc -n ${DEV_PROJECT}
oc delete configmap ${APP}-config -n ${DEV_PROJECT}

echo Setting up ${APP} for ${DEV_PROJECT}
oc new-build --binary=true --labels=app=${APP} --name=${APP} jboss-eap70-openshift:1.6 -n ${DEV_PROJECT}
oc new-app -f ${APP}-dc.yaml --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
oc set volume dc/${APP} --add --name=${APP}-config-vol --mount-path=/opt/eap/standalone/configuration/${APP}-config --configmap-name=${APP}-config -n ${DEV_PROJECT}
oc expose dc ${APP} --port 8080 -n ${DEV_PROJECT}
oc expose svc ${APP} -n ${DEV_PROJECT}

oc delete all -l app=${APP} -n ${PROD_PROJECT}
oc delete is,bc,configmap ${APP} -n ${PROD_PROJECT}
oc delete template ${APP}-dev-dc -n ${PROD_PROJECT}
oc delete configmap ${APP}-config -n ${PROD_PROJECT}

echo Setting up ${APP} for ${PROD_PROJECT}
oc new-build --binary=true --labels=app=${APP} --name=${APP} jboss-eap70-openshift:1.6 -n ${PROD_PROJECT}
oc new-app -f ${APP}-dc.yaml --allow-missing-imagestream-tags=true -n ${PROD_PROJECT}
oc set volume dc/${APP} --add --name=${APP}-config-vol --mount-path=/opt/eap/standalone/configuration/${APP}-config --configmap-name=${APP}-config -n ${PROD_PROJECT}
oc expose dc ${APP} --port 8080 -n ${PROD_PROJECT}
oc expose svc ${APP} -n ${PROD_PROJECT}
