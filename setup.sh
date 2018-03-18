#!/usr/bin/env bash

. ./env.sh

oc login https://${IP}:8443 -u $USER

oc delete project $CICD_PROJECT
oc new-project $CICD_PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $CICD_PROJECT 2> /dev/null
done

oc delete project $DEV_PROJECT
oc new-project $DEV_PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $DEV_PROJECT 2> /dev/null
done

oc policy add-role-to-user edit system:serviceaccount:${CICD_PROJECT}:jenkins -n ${DEV_PROJECT}
oc policy add-role-to-user edit system:serviceaccount:${CICD_PROJECT}:default -n ${DEV_PROJECT}

for APP in "mlbparks nationalparks parksmap"
do
    echo Setting up ${APP}
    oc new-build --binary=true --name=${APP} jboss-eap70-openshift:1.6 -n ${DEV_PROJECT}
    oc new-app ${DEV_PROJECT}/${APP}:0.0-0 --name=${APP} --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
    oc set triggers dc/${APP} --remove-all -n ${DEV_PROJECT}
    oc expose dc ${APP} --port 8080 -n ${DEV_PROJECT}
    oc expose svc ${APP} -n ${DEV_PROJECT}
done

oc delete project $PROD_PROJECT
oc new-project $PROD_PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROD_PROJECT 2> /dev/null
done

oc policy add-role-to-user edit system:serviceaccount:${CICD_PROJECT}:jenkins -n ${PROD_PROJECT}
oc policy add-role-to-user edit system:serviceaccount:${CICD_PROJECT}:default -n ${PROD_PROJECT}

for APP in "mlbparks nationalparks parksmap"
do
    echo Setting up ${APP}
    oc new-app ${PROD_PROJECT}/${APP}:0.0 --name=${APP} --allow-missing-imagestream-tags=true -n ${PROD_PROJECT}
    oc set triggers dc/${APP} --remove-all -n ${PROD_PROJECT}
    oc expose dc ${APP} --port 8080 -n ${PROD_PROJECT}
    oc expose svc ${APP} -n ${PROD_PROJECT}
done