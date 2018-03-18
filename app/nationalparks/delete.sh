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
  -X POST https://${JENKINS}/job/nationalparks/doDelete


