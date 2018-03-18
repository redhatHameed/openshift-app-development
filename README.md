#Openshift Continuous Delivery Demo

## Get Openshift

First setup an Openshift cluster, a guide for this is available here :

https://github.com/justindav1s/openshift-ansible-on-openstack

##Configure Openshift for CICD

Scripts in this repo are designed acheive a number of goals :

1. Setup a CICD environment, in its own openshift project "**cicd**", this consists of
    1. **postgresql**, a database required by other tools.
    2. **gogs**, a lightweight git server, this requires a db schema
    3. **sonarqube**, this analsyses an provides insights into code quality
    4. **nexus**, a maven repository for shared resources
    5. **jenkins**, a build & deploy automation engine

    This infrastructure chained together correctly can facilitate app development and delivery automation for any modern programming language
 
2. Deliver a continuous integration and deployment process for three java based microservices. Building, testing and deploying Openshift platform. The source code to be deployed can be found here :
https://github.com/wkulhanek/ParksMap

    The microservices are:
    1. **mlbparks** - a REST API to a database of baseball stadiums
    2. **nationalparks** - a REST API to a database of nation parks
    3. **parksmap** - a mapping eapplications that leverages these APIs

###Setting the environment

1. In the homwork folder run setup.sh, this :
    1. sets up three openshift projects
        1. cicd - hosts the CICD components, jenkins, sonar, nexus etc
        2. mitzicom-dev - this hosts the development version of the application for testing purposes.
        3. mitzicom-prod - this hosts the production version of the application, changes tested in dev are promoted to here.
        4. configures openshift security, so that Jenkins can orchestrate changes in the mitzicom-dev and mitzicom-prod projects
    

