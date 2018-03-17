#!/usr/bin/env bash

#. ../env.sh

USER=justindav1s
GOGS=https://gogs-cicd.apps.ocp.datr.eu
TOKEN=d999c769e69bd9fa4b35b0899010e29d14c40796
ORG=mitzicom

curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/repos/${ORG}/mlbparks?token=${TOKEN}
curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/repos/${ORG}/nationalparks?token=${TOKEN}
curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/repos/${ORG}/parksmap2?token=${TOKEN}
curl -v -H "Content-Type: application/json" -X DELETE ${GOGS}/api/v1/admin/users/${USER}/orgs?token=${TOKEN}