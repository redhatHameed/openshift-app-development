#!/usr/bin/env bash

. ../env.sh

oc login https://${IP}:8443 -u $USER

oc delete project $DEV_PROJECT
oc new-project $DEV_PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $DEV_PROJECT 2> /dev/null
done

oc policy add-role-to-user edit system:serviceaccount:${CICD_PROJECT}:jenkins -n ${DEV_PROJECT}
oc policy add-role-to-user edit system:serviceaccount:${CICD_PROJECT}:default -n ${DEV_PROJECT}


APP=mlbparks
S2I_IMAGE=jboss-eap70-openshift:1.6
echo Setting up ${APP} for ${DEV_PROJECT}
oc new-build --binary=true --labels=app=${APP} --name=${APP} ${S2I_IMAGE} -n ${DEV_PROJECT}
oc new-app -f ${APP}/${APP}-dev-dc.yaml --allow-missing-imagestream-tags=true -n ${DEV_PROJECT}
oc set volume dc/${APP} --add --name=${APP}-config-vol --mount-path=/config --configmap-name=${APP}-config -n ${DEV_PROJECT}
oc expose dc ${APP} --port 8080 -n ${DEV_PROJECT}
oc expose svc ${APP} -l type=parksmap-backend -n ${DEV_PROJECT}