#!/usr/bin/env bash


./build_ocp_resources.sh mlbparks jboss-eap70-openshift:1.6

./build_ocp_resources.sh nationalparks redhat-openjdk18-openshift:1.2

./build_ocp_resources.sh parksmap redhat-openjdk18-openshift:1.2