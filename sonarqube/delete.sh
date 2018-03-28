#!/usr/bin/env bash

. ../env.sh

oc project $CICD_PROJECT

oc delete all -l app=sonarqube

oc delete serviceaccounts sonarqube

oc delete pvc sonarqube-data sonarqube-extensions