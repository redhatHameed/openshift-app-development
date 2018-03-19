#!/usr/bin/env bash


APP=$1

. ../env.sh

oc login https://${IP}:8443 -u $USER

oc project ${DEV_PROJECT}

oc delete all -l app=${APP} -n ${DEV_PROJECT}
oc delete is,bc,configmap ${APP} -n ${DEV_PROJECT}
oc delete template ${APP}-dev-dc -n ${DEV_PROJECT}
oc delete configmap ${APP}-config -n ${DEV_PROJECT}

echo Setting up ${APP} for ${DEV_PROJECT}
oc new-build --binary=true --labels=app=${APP} --name=${APP} jboss-eap70-openshift:1.6 -n ${DEV_PROJECT}
oc new-app -f ${APP}-dev-dc.yaml --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
oc set volume dc/${APP} --add --name=${APP}-config-vol --mount-path=/opt/eap/standalone/configuration/${APP}-config --configmap-name=${APP}-config -n ${DEV_PROJECT}
oc expose dc ${APP} --port 8080 -n ${DEV_PROJECT}
oc expose svc ${APP} -n ${DEV_PROJECT}

oc delete all -l app=${APP} -n ${PROD_PROJECT}
oc delete is,bc,configmap ${APP}-${COLOUR} -n ${PROD_PROJECT}
oc delete template ${APP}-prod-dc -n ${PROD_PROJECT}
oc delete configmap ${APP}-${COLOUR}-config -n ${PROD_PROJECT}

echo Setting up ${APP} for ${PROD_PROJECT}
COLOUR=blue
oc new-build --binary=true --labels=app=${APP} --name=${APP}-${COLOUR} jboss-eap70-openshift:1.6 -n ${PROD_PROJECT}
oc new-app -f ${APP}-prod-dc.yaml --allow-missing-imagestream-tags=true -p BLUE_OR_GREEN=${COLOUR} -n ${PROD_PROJECT}
oc set volume dc/${APP}-${COLOUR} --add --name=${APP}-${COLOUR}-config-vol --mount-path=/opt/eap/standalone/configuration/${APP}-config --configmap-name=${APP}-${COLOUR}-config -n ${PROD_PROJECT}
oc expose dc ${APP}-${COLOUR} --port 8080 -n ${PROD_PROJECT}
oc expose svc ${APP}-${COLOUR} -n ${PROD_PROJECT}

COLOUR=green
oc new-build --binary=true --labels=app=${APP} --name=${APP}-${COLOUR} jboss-eap70-openshift:1.6 -n ${PROD_PROJECT}
oc new-app -f ${APP}-prod-dc.yaml --allow-missing-imagestream-tags=true -p BLUE_OR_GREEN=${COLOUR} -n ${PROD_PROJECT}
oc set volume dc/${APP}-${COLOUR} --add --name=${APP}-${COLOUR}-config-vol --mount-path=/opt/eap/standalone/configuration/${APP}-config --configmap-name=${APP}-${COLOUR}-config -n ${PROD_PROJECT}
oc expose dc ${APP}-${COLOUR} --port 8080 -n ${PROD_PROJECT}
oc expose svc ${APP}-${COLOUR} -n ${PROD_PROJECT}

oc expose svc ${APP}-${COLOUR} --hostname=mlbparks.apps.ocp.datr.eu --name=${APP} -n ${PROD_PROJECT}