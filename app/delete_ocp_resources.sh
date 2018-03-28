#!/usr/bin/env bash

. ../mitzicom/env.sh

oc login https://${IP}:8443 -u $USER

for APP in mlbparks nationalparks parksmap
do
    oc project ${DEV_PROJECT}
    oc delete all -l app=${APP} -n ${DEV_PROJECT}
    oc delete is,bc,dc,svc,route ${APP} -n ${DEV_PROJECT}
    oc delete template ${APP}-dev-dc -n ${DEV_PROJECT}
    oc delete configmap ${APP}-config -n ${DEV_PROJECT}

    oc project ${PROD_PROJECT}
    oc delete all -l app=${APP} -n ${PROD_PROJECT}
    oc delete template ${APP}-prod-dc -n ${PROD_PROJECT}

    for COLOUR in blue green
    do
        oc delete is,bc,dc,svc,route ${APP}-${COLOUR} -n ${PROD_PROJECT}
        oc delete configmap ${APP}-${COLOUR}-config -n ${PROD_PROJECT}
    done
done