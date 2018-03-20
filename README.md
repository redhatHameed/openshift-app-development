# Openshift Continuous Delivery Demo

## Get Openshift

First setup an Openshift cluster, a guide for this is available here :

https://github.com/justindav1s/openshift-ansible-on-openstack

## Configure Openshift for CICD

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
    2. **nationalparks** - a REST API to a database of national parks
    3. **parksmap** - a mapping eapplications that leverages these APIs

### Setting the environment

1. First run **setup.sh**, this :
    1. sets up three openshift projects
        1. **cicd** - hosts the CICD components, jenkins, sonar, nexus etc
        2. **mitzicom-dev** - this hosts the development version of the application for testing purposes.
        3. **mitzicom-prod** - this hosts the production version of the application, changes tested in dev are promoted to here.
     2. configures openshift security, so that Jenkins can orchestrate changes in the mitzicom-dev and mitzicom-prod projects.
     3. configures build and deployment configurations in mitzicom-dev, mitzicom-prod, these will be used by jenkins later.
     
2. In the **postgresql** folder run the **deploy.sh** script, this deploys a postgresql database with persistent storage
3. In the **gogs** folder run the **deploy.sh** script, this deploys the gogs version control application
4. In the **sonarqube** folder run the **deploy.sh** script, this deploys the sonarqube code quality analysis application    
5. In the **nexus** folder run the **deploy.sh** script, this deploys the nexus maven repository management application
    1. next run the **setup_nexus3.sh** script, this adds extra repos to nexus and also allows it to be a docker repository
        ``$ ./setup_nexus3.sh <nexus username> <nexus password> <nexus URL>``
    2. next run **add-docker-registry-route.sh** script to expose the docker registry capability on your network        
6. In the **jenkins** folder run the **deploy.sh** script, this deploys the Jenkins process automation engine.
    1. Go into jenkins and configure your gogs credentials, these will be used by pipelines
    2. Untick **Enable script security for Job DSL scripts** on the **Manage Jenkins > Configure Global Security** page
7. In the **repos** folder run the **setup.sh** script, this clones https://github.com/wkulhanek/ParksMap and then splits that repo into three others and imports them into gogs :
    1. https://gogs-cicd.apps.your.ocp.domain/mitzicom/parksmap    
    2. https://gogs-cicd.apps.your.ocp.domain/mitzicom/nationalparks
    3. https://gogs-cicd.apps.your.ocp.domain/mitzicom/mlbparks
8. In the **skopeo-jenkins-slave** folder, the script **build-deploy-skope-slave-image.sh** does a docker build of a Jenkins slave incorporating skope, and deploys it to Openshifts image registry.

This concludes environment setup the remainder of this guide concerns setup of the runtime environment of the ParksMap app.

##Setting Up   
    
8. In the **app/mlbparks** folder run the **setup.sh** script, this creates a jenkins pipeline job, that points to **app/mlbparks/pipeline.groovy** file in this repo
9. In the **app/nationalparks** folder run the **setup.sh** script, this creates a jenkins pipeline job, that points to **app/nationalparks/pipeline.groovy** file in this repo
10. In the **app/parksmap** folder run the **setup.sh** script, this creates a jenkins pipeline job, that points to **app/parksmap/pipeline.groovy** file in this repo
11. In the **app/mongodb** folder run :
    1. **dev-deploy.sh**, this deploys a single replica mogodb statefulset with persistent storage into the **mitzicom-dev** project
    2. **prod-deploy.sh**, this deploys a 3 replica mogodb statefulset with persistent storage into the **mitzicom-prod** project
    
### Running the CICD process

Having done the preparation above, we now have three seperate and independent pipelines that deploy our microservices in line with the blue-green deployment paradigm described here :
  - https://martinfowler.com/bliki/BlueGreenDeployment.html
  
Deployments are triggered by interacting with the jobs in Jenkins and by git pushs to the master branches of each microservice in gogs. 
