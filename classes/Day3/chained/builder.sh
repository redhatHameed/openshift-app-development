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

oc import-image jorgemoralespou/s2i-go --confirm

oc new-build s2i-go~https://github.com/tonykay/ose-chained-builds --context-dir="/go-scratch/hello_world" --name=builder

sleep 20

oc new-build --name=runtime \
   --docker-image=scratch \
   --source-image=builder \
   --source-image-path=/opt/app-root/src/go/src/main/main:. \
   --dockerfile=$'FROM scratch\nCOPY main /main\nEXPOSE 8080\nENTRYPOINT ["/main"]'

sleep 20

oc new-app runtime --name=my-application

oc expose svc/my-application


