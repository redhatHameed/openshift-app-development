#!/usr/bin/env bash

USER=justin-admin
TOKEN=16f2aff7615fb87337d2d460b5eda36f
JENKINS=jenkins-cicd.apps.ocp.datr.eu

CRUMB_JSON=$(curl -s "https://${USER}:${TOKEN}@${JENKINS}/crumbIssuer/api/json")

echo CRUMB_JSON=$CRUMB_JSON
CRUMB=$(echo $CRUMB_JSON | jq -r .crumb)
echo CRUMB=$CRUMB

curl -v -H "Content-Type: text/xml" \
  --user ${USER}:${TOKEN} \
  -H Jenkins-Crumb:${CRUMB} \
  --data-binary @mlbparks/config.xml \
  -X POST https://${JENKINS}/createItem?name=mlbparks


