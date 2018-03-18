#!/usr/bin/env bash

#. ../env.sh

USER=justindav1s
GOGS=https://gogs-cicd.apps.ocp.datr.eu
TOKEN=a91acd6c02a003988cd526fe3152362b1c2800dc
ORG=mitzicom

curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/repos/${ORG}/mlbparks?token=${TOKEN}
curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/repos/${ORG}/nationalparks?token=${TOKEN}
curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/repos/${ORG}/parksmap?token=${TOKEN}
curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/admin/users/${USER}/orgs?token=${TOKEN}