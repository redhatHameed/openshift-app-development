#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-tasks-dev
JENKINS_PROJECT=jnd-jenkins

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done


# Set up Dev Project
oc policy add-role-to-user edit system:serviceaccount:${JENKINS_PROJECT}:jenkins -n ${PROJECT}
oc policy add-role-to-user edit system:serviceaccount:${JENKINS_PROJECT}:default -n ${PROJECT}

# Set up Dev Application
oc new-build --binary=true --name="tasks" jboss-eap70-openshift:1.6 -n ${PROJECT}
oc new-app ${PROJECT}/tasks:0.0-0 --name=tasks --allow-missing-imagestream-tags=true -n ${PROJECT}
oc set triggers dc/tasks --remove-all -n ${PROJECT}
oc expose dc tasks --port 8080 -n ${PROJECT}
oc expose svc tasks -n ${PROJECT}
oc create configmap tasks-config --from-literal="application-users.properties=Placeholder" --from-literal="application-roles.properties=Placeholder" -n ${PROJECT}
oc set volume dc/tasks --add --name=jboss-config --mount-path=/opt/eap/standalone/configuration/application-users.properties --sub-path=application-users.properties --configmap-name=tasks-config -n ${PROJECT}
oc set volume dc/tasks --add --name=jboss-config1 --mount-path=/opt/eap/standalone/configuration/application-roles.properties --sub-path=application-roles.properties --configmap-name=tasks-config -n ${PROJECT}

