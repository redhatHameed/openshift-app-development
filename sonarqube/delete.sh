#!/usr/bin/env bash

. ../env.sh

oc delete all -l app=sonarqube

oc delete serviceaccounts sonarqube

oc delete pvc sonarqube-data