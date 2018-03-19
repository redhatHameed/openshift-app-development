#!/usr/bin/env bash

JENKINS_USER=justin-admin
JENKINS_TOKEN=ef09f2fdff580b687a6a05cad57c9429
JENKINS=jenkins-cicd.apps.ocp.datr.eu

CRUMB_JSON=$(curl -s "https://${JENKINS_USER}:${JENKINS_TOKEN}@${JENKINS}/crumbIssuer/api/json")

echo CRUMB_JSON=$CRUMB_JSON
CRUMB=$(echo $CRUMB_JSON | jq -r .crumb)
echo CRUMB=$CRUMB

curl -v -H "Content-Type: text/xml" \
  --user ${JENKINS_USER}:${JENKINS_TOKEN} \
  -H Jenkins-Crumb:${CRUMB} \
  -X POST https://${JENKINS}/job/mlbparks/doDelete

curl -v -H "Content-Type: text/xml" \
  --user ${JENKINS_USER}:${JENKINS_TOKEN} \
  -H Jenkins-Crumb:${CRUMB} \
  -X POST https://${JENKINS}/job/nationalparks/doDelete

curl -v -H "Content-Type: text/xml" \
  --user ${JENKINS_USER}:${JENKINS_TOKEN} \
  -H Jenkins-Crumb:${CRUMB} \
  -X POST https://${JENKINS}/job/parksmap/doDelete



. ../env.sh

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