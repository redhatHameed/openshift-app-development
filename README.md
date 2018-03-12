# ocp-appdev

http://etherpad-opentlc-shared.apps.na37.openshift.opentlc.com/p/AdvPaaS_Development_FRA_2018_03_12


oc patch route bluegreen -p '{"spec": {"to": [{"kind": "Service","name": "green","weight": 100}}}'
oc patch route bluegreen -p '{"spec": {"to": [{"kind": "Service","name": "blue","weight": 100}}}'

oc set route-backends bluegreen blue=0 green=100

oc set route-backends bluegreen blue=100 green=0

Readiness


  # Clear both readiness and liveness probes off all containers
  oc set probe dc/registry --remove --readiness --liveness
  
  # Set an exec action as a liveness probe to run 'echo ok'
  oc set probe dc/registry --liveness -- echo ok
  
  # Set a readiness probe to try to open a TCP socket on 3306
  oc set probe rc/mysql --readiness --open-tcp=3306
  
  # Set an HTTP readiness probe for port 8080 and path /healthz over HTTP on the pod IP
  oc set probe dc/blue --readiness --get-url=http://:8080/item.php
  oc set probe dc/green --readiness --get-url=http://:8080/item.php
  
  # Set an HTTP readiness probe over HTTPS on 127.0.0.1 for a hostNetwork pod
  oc set probe dc/router --readiness --get-url=https://127.0.0.1:1936/stats
  
  # Set only the initial-delay-seconds field on all deployments
  oc set probe dc --all --readiness --initial-delay-seconds=30
  
  
  


