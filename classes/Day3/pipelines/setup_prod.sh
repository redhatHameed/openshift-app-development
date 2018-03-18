#!/usr/bin/env bash

oc login https://master.fra.example.opentlc.com -u jusdavis-redhat.com

PROJECT=jnd-tasks-prod
DEV_PROJECT=jnd-tasks-dev
JENKINS_PROJECT=jnd-jenkins

oc delete project $PROJECT
oc new-project $PROJECT 2> /dev/null
while [ $? \> 0 ]; do
    sleep 1
    printf "."
    oc new-project $PROJECT 2> /dev/null
done


# Set up Production Project
oc policy add-role-to-group system:image-puller system:serviceaccounts:${PROJECT} -n ${DEV_PROJECT}
oc policy add-role-to-user edit system:serviceaccount:${JENKINS_PROJECT}:jenkins -n ${PROJECT}
oc policy add-role-to-user edit system:serviceaccount:${JENKINS_PROJECT}:default -n ${PROJECT}

# Create Blue Application
oc new-app ${DEV_PROJECT}/tasks:0.0 --name=tasks-blue --allow-missing-imagestream-tags=true -n ${PROJECT}
oc set triggers dc/tasks-blue --remove-all -n ${PROJECT}
oc expose dc tasks-blue --port 8080 -n ${PROJECT}
oc create configmap tasks-blue-config --from-literal="application-users.properties=Placeholder" --from-literal="application-roles.properties=Placeholder" -n ${PROJECT}
oc set volume dc/tasks-blue --add --name=jboss-config --mount-path=/opt/eap/standalone/configuration/application-users.properties --sub-path=application-users.properties --configmap-name=tasks-blue-config -n ${PROJECT}
oc set volume dc/tasks-blue --add --name=jboss-config1 --mount-path=/opt/eap/standalone/configuration/application-roles.properties --sub-path=application-roles.properties --configmap-name=tasks-blue-config -n ${PROJECT}

# Create Green Application
oc new-app ${DEV_PROJECT}/tasks:0.0 --name=tasks-green --allow-missing-imagestream-tags=true -n ${PROJECT}
oc set triggers dc/tasks-green --remove-all -n ${PROJECT}
oc expose dc tasks-green --port 8080 -n ${PROJECT}
oc create configmap tasks-green-config --from-literal="application-users.properties=Placeholder" --from-literal="application-roles.properties=Placeholder" -n ${PROJECT}
oc set volume dc/tasks-green --add --name=jboss-config --mount-path=/opt/eap/standalone/configuration/application-users.properties --sub-path=application-users.properties --configmap-name=tasks-green-config -n ${PROJECT}
oc set volume dc/tasks-green --add --name=jboss-config1 --mount-path=/opt/eap/standalone/configuration/application-roles.properties --sub-path=application-roles.properties --configmap-name=tasks-green-config -n ${PROJECT}

# Expose Blue service as route to make blue application active
oc expose svc/tasks-blue --name tasks -n ${PROJECT}
