#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-builds

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done



#oc new-app --template=eap70-basic-s2i \
#     --param APPLICATION_NAME=tasks \
#     --param SOURCE_REPOSITORY_URL=http://gogs.jnd-gogs.svc.cluster.local:3000/CICDLabs/openshift-tasks-private.git \
#     --param SOURCE_REPOSITORY_REF=master \
#     --param CONTEXT_DIR=/ \
#     --param MAVEN_MIRROR_URL=http://nexus-jnd-nexus.apps.fra.example.opentlc.com/repository/maven-all-public
#
#oc cancel-build tasks
#
#oc create -f gogs-secret.yaml
#oc set build-secret --source bc/tasks gogs-secret
#
#oc start-build tasks

oc new-app redhat-openjdk18-openshift:1.2~https://github.com/wkulhanek/ola.git

#oc new-app -f template.yaml
