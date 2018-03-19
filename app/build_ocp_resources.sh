#!/usr/bin/env bash


APP=$1
S2I_IMAGE=$2

. ../env.sh

oc login https://${IP}:8443 -u $USER

#oc project ${DEV_PROJECT}
#
#oc delete all -l app=${APP} -n ${DEV_PROJECT}
#oc delete is,bc,dc,svc,route ${APP} -n ${DEV_PROJECT}
#oc delete template ${APP}-dev-dc -n ${DEV_PROJECT}
#oc delete configmap ${APP}-config -n ${DEV_PROJECT}
#
#echo Setting up ${APP} for ${DEV_PROJECT}
#oc new-build --binary=true --labels=app=${APP} --name=${APP} ${S2I_IMAGE} -n ${DEV_PROJECT}
#oc new-app -f ${APP}/${APP}-dev-dc.yaml --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
#oc set volume dc/${APP} --add --name=${APP}-config-vol --mount-path=/config --configmap-name=${APP}-config -n ${DEV_PROJECT}
#oc expose dc ${APP} --port 8080 -n ${DEV_PROJECT}
#oc expose svc ${APP} -l type=parksmap-backend -n ${DEV_PROJECT}


echo Setting up ${APP} for ${PROD_PROJECT}

oc project ${PROD_PROJECT}

oc delete all -l app=${APP} -n ${PROD_PROJECT}

COLOUR=blue

oc delete template ${APP}-prod-dc -n ${PROD_PROJECT}
oc delete is,bc,dc,svc,route ${APP}-${COLOUR} -n ${PROD_PROJECT}
oc delete configmap ${APP}-${COLOUR}-config -n ${PROD_PROJECT}

oc new-build --binary=true --labels=app=${APP} --name=${APP}-${COLOUR} ${S2I_IMAGE} -n ${PROD_PROJECT}
oc new-app -f ${APP}/${APP}-prod-dc.yaml --allow-missing-imagestream-tags=true -p BLUE_OR_GREEN=${COLOUR} -n ${PROD_PROJECT}
oc set volume dc/${APP}-${COLOUR} --add --name=${APP}-${COLOUR}-config-vol --mount-path=/config --configmap-name=${APP}-${COLOUR}-config -n ${PROD_PROJECT}
oc expose dc ${APP}-${COLOUR} --port 8080 -n ${PROD_PROJECT}
oc expose svc ${APP}-${COLOUR} -n ${PROD_PROJECT}

COLOUR=green
oc delete template ${APP}-prod-dc -n ${PROD_PROJECT}
oc delete is,bc,dc,svc,route ${APP}-${COLOUR} -n ${PROD_PROJECT}
oc delete configmap ${APP}-${COLOUR}-config -n ${PROD_PROJECT}

oc new-build --binary=true --labels=app=${APP} --name=${APP}-${COLOUR} ${S2I_IMAGE} -n ${PROD_PROJECT}
oc new-app -f ${APP}/${APP}-prod-dc.yaml --allow-missing-imagestream-tags=true -p BLUE_OR_GREEN=${COLOUR} -n ${PROD_PROJECT}
oc set volume dc/${APP}-${COLOUR} --add --name=${APP}-${COLOUR}-config-vol --mount-path=/config --configmap-name=${APP}-${COLOUR}-config -n ${PROD_PROJECT}
oc expose dc ${APP}-${COLOUR} --port 8080 -n ${PROD_PROJECT}
oc expose svc ${APP}-${COLOUR} -n ${PROD_PROJECT}

if [ ${APP} == "parksmap" ]
then
    oc expose svc ${APP}-${COLOUR} --hostname=${APP}.apps.ocp.datr.eu --name=${APP} -n ${PROD_PROJECT}
else
    oc expose svc ${APP}-${COLOUR} -l type=parksmap-backend --hostname=${APP}.apps.ocp.datr.eu --name=${APP} -n ${PROD_PROJECT}