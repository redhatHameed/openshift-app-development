#!groovy

// Run this pipeline on the custom Maven Slave ('maven-appdev')
// Maven Slaves have JDK and Maven already installed
// 'maven-appdev' has skopeo installed as well.
node('maven-appdev') {
    // Define Maven Command. Make sure it points to the correct
    // settings for our Nexus installation (use the service to
    // bypass the router). The file nexus_openshift_settings.xml
    // needs to be in the Source Code repository.
    def mvnCmd = "mvn -s ./nexus_openshift_settings.xml"
    // Checkout Source Code
    stage('Checkout Source') {
        git credentialsId: 'jnd-gogs2', url: 'http://gogs-jnd-gogs.apps.fra.example.opentlc.com/CICDLabs/openshift-tasks-private.git'
    }

    // The following variables need to be defined at the top level
    // and not inside the scope of a stage - otherwise they would not
    // be accessible from other stages.
    // Extract version and other properties from the pom.xml
    def groupId    = getGroupIdFromPom("pom.xml")
    def artifactId = getArtifactIdFromPom("pom.xml")
    def version    = getVersionFromPom("pom.xml")
    def packaging    = getPackagingFromPom("pom.xml")

    // Set the tag for the development image: version + build number
    def devTag  = "0.0-0"
    // Set the tag for the production image: version
    def prodTag = "0.0"

//    // Using Maven build the war file
//    // Do not run tests in this step
//    stage('Build war') {
//        echo "Building version ${version}"
//        sh "mvn -B -s nexus_settings.xml clean package -DskipTests"
//    }
//
//    // Using Maven run the unit tests
//    stage('Unit Tests') {
//        echo "Running Unit Tests"
//        sh "mvn -B -s nexus_settings.xml test"
//    }
//
//    // Using Maven call SonarQube for Code Analysis
//    stage('Code Analysis') {
//        echo "Running Code Analysis"
//        sh "mvn -B -s nexus_settings.xml sonar:sonar -DskipTests -Dsonar.host.url=http://sonarqube-jnd-sonarqube.apps.fra.example.opentlc.com"
//    }
//
//    // Publish the built war file to Nexus
//    stage('Publish to Nexus') {
//        echo "Publish to Nexus"
//        sh "mvn -B -s nexus_settings.xml deploy -DskipTests"
//    }

    // Build the OpenShift Image in OpenShift and tag it.
    stage('Build and Tag OpenShift Image') {
        echo "Building OpenShift container image tasks:${devTag}"
        echo "Group ID : ${groupId}"
        echo "Artifact ID : ${artifactId}"
        echo "Version : ${version}"
        echo "Packaging : ${packaging}"

        sh "mvn -B -s nexus_settings.xml dependency:copy -DstripVersion=true -Dartifact=${groupId}:${artifactId}:${version}:${packaging} -DoutputDirectory=."
        sh "cp \$(find . -type f -name \"${artifactId}-*.${packaging}\")  ${artifactId}.${packaging}"
        sh "pwd; ls -ltr"
        sh "oc start-build tasks --follow --from-file=${artifactId}.${packaging} -n jnd-tasks-dev"

        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: 'tasks', destTag: "${devTag}", destinationAuthToken: '', destinationNamespace: 'jnd-tasks-dev', namespace: 'jnd-tasks-dev', srcStream: 'tasks', srcTag: 'latest', verbose: 'false'
    }

    // Deploy the built image to the Development Environment.
    stage('Deploy to Dev') {
        echo "Deploying container image to Development Project"
        //sh "oc rollout latest dc/tasks -n jnd-tasks-dev"
        //sh "oc expose service/tasks -n jnd-tasks-dev"
        openshiftDeploy apiURL: '', authToken: '', depCfg: 'tasks', namespace: 'jnd-tasks-dev', verbose: 'false', waitTime: '', waitUnit: 'sec'
    }

    // Run Integration Tests in the Development Environment.
    stage('Integration Tests') {
        echo "Running Integration Tests"
        // TBD
    }

    // Copy Image to Nexus Docker Registry
    stage('Copy Image to Nexus Docker Registry') {
        echo "Copy image to Nexus Docker Registry"
        // TBD
    }

    // Blue/Green Deployment into Production
    // -------------------------------------
    // Do not activate the new version yet.
    def destApp   = "tasks-green"
    def activeApp = ""

    stage('Blue/Green Production Deployment') {
        // TBD
    }

    stage('Switch over to new Version') {
        // TBD
        echo "Switching Production application to ${destApp}."
        // TBD
    }
}

// Convenience Functions to read variables from the pom.xml
// Do not change anything below this line.
// --------------------------------------------------------
def getVersionFromPom(pom) {
    def matcher = readFile(pom) =~ '<version>(.+)</version>'
    matcher ? matcher[0][1] : null
}
def getGroupIdFromPom(pom) {
    def matcher = readFile(pom) =~ '<groupId>(.+)</groupId>'
    matcher ? matcher[0][1] : null
}
def getArtifactIdFromPom(pom) {
    def matcher = readFile(pom) =~ '<artifactId>(.+)</artifactId>'
    matcher ? matcher[0][1] : null
}
def getPackagingFromPom(pom) {
    def matcher = readFile(pom) =~ '<packaging>(.+)</packaging>'
    matcher ? matcher[0][1] : null
}