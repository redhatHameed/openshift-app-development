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
//    stage('Build and Tag OpenShift Image') {
//        echo "Building OpenShift container image tasks:${devTag}"
//        echo "Group ID : ${groupId}"
//        echo "Artifact ID : ${artifactId}"
//        echo "Version : ${version}"
//        echo "Packaging : ${packaging}"
//
//        sh "mvn -B -s nexus_settings.xml dependency:copy -DstripVersion=true -Dartifact=${groupId}:${artifactId}:${version}:${packaging} -DoutputDirectory=."
//        sh "cp \$(find . -type f -name \"${artifactId}-*.${packaging}\")  ${artifactId}.${packaging}"
//        sh "pwd; ls -ltr"
//        sh "oc start-build tasks --follow --from-file=${artifactId}.${packaging} -n jnd-tasks-dev"
//        openshiftVerifyBuild apiURL: '', authToken: '', bldCfg: 'tasks', checkForTriggeredDeployments: 'true', namespace: 'jnd-tasks-dev', verbose: 'false', waitTime: ''
//        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: 'tasks', destTag: "${devTag}", destinationAuthToken: '', destinationNamespace: 'jnd-tasks-dev', namespace: 'jnd-tasks-dev', srcStream: 'tasks', srcTag: 'latest', verbose: 'false'
//    }
//
//    // Deploy the built image to the Development Environment.
//    stage('Deploy to Dev') {
//        echo "Deploying container image to Development Project"
//        sh "oc set image dc/tasks tasks=docker-registry.default.svc:5000/jnd-tasks-dev/tasks:${devTag} -n jnd-tasks-dev"
//        sh "oc delete configmap tasks-config -n jnd-tasks-dev"
//        sh "oc create configmap tasks-config --from-file=./configuration/application-users.properties --from-file=./configuration/application-roles.properties -n jnd-tasks-dev"
//        openshiftDeploy apiURL: '', authToken: '', depCfg: 'tasks', namespace: 'jnd-tasks-dev', verbose: 'false', waitTime: '', waitUnit: 'sec'
//        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: 'tasks', namespace: 'jnd-tasks-dev', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '', waitUnit: 'sec'
//    }

//    // Run Integration Tests in the Development Environment.
//    stage('Integration Tests') {
//        echo "Running Integration Tests"
//        //sleep(30)
//        openshiftVerifyService apiURL: '', authToken: '', namespace: 'jnd-tasks-dev', svcName: 'tasks', verbose: 'false'
//        echo "Checking for homepage ..."
//        def curlget = "curl -f http://tasks-jnd-tasks-dev.apps.fra.example.opentlc.com/index.jsp".execute().with{
//            def output = new StringWriter()
//            def error = new StringWriter()
//            it.waitForProcessOutput(output, error)
//            println it.exitValue()
//            assert it.exitValue() == 0: "$error"
//        }
//        echo "Posting to service ..."
//        def curlpost = "curl -i -f -u tasks:redhat1 -X POST http://tasks-jnd-tasks-dev.apps.fra.example.opentlc.com/ws/tasks/integration_test_1".execute().with{
//            def output = new StringWriter()
//            def error = new StringWriter()
//            it.waitForProcessOutput(output, error)
//            println it.exitValue()
//            assert it.exitValue() == 0: "$error"
//        }
//        echo "Getting from service ..."
//        def curlget2 = "curl -i -f -u tasks:redhat1 -X GET http://tasks-jnd-tasks-dev.apps.fra.example.opentlc.com/ws/tasks/1".execute().with{
//            def output = new StringWriter()
//            def error = new StringWriter()
//            it.waitForProcessOutput(output, error)
//            println it.exitValue()
//            assert it.exitValue() == 0: "$error"
//        }
//        echo "Deleteing from service ..."
//        def curldel = "curl -i -f -u tasks:redhat1 -X DELETE http://tasks-jnd-tasks-dev.apps.fra.example.opentlc.com/ws/tasks/1".execute().with{
//            def output = new StringWriter()
//            def error = new StringWriter()
//            it.waitForProcessOutput(output, error)
//            println it.exitValue()
//            assert it.exitValue() == 0: "$error"
//        }
//
//        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: 'tasks', destTag: "${prodTag}", destinationAuthToken: '', destinationNamespace: 'jnd-tasks-prod', namespace: 'jnd-tasks-dev', srcStream: 'tasks', srcTag: "${devTag}", verbose: 'false'
//    }

//    // Copy Image to Nexus Docker Registry
//    stage('Copy Image to Nexus Docker Registry') {
//        echo "Copy image to Nexus Docker Registry"
//        sh"skopeo \\\n" +
//                "    --insecure-policy \\\n" +
//                "    copy \\\n" +
//                "    --src-creds=jusdavis-redhat.com:\$(oc whoami -t) \\\n" +
//                "    --dest-creds=admin:admin123 \\\n" +
//                "    --src-tls-verify=false \\\n" +
//                "    --dest-tls-verify=false \\\n" +
//                "    docker://docker-registry-default.apps.fra.example.opentlc.com/jnd-jenkins/jenkins-slave-maven-jnd:latest \\\n" +
//                "    docker://registry-jnd-nexus.apps.fra.example.opentlc.com/jnd-jenkins/jenkins-slave-maven-jnd:latest"
//    }

    // Blue/Green Deployment into Production
    // -------------------------------------
    // Do not activate the new version yet.
    def destApp   = "tasks-green"
    def activeApp = ""

//    stage('Blue/Green Production Deployment') {
//        sh "oc set image dc/${destApp} ${destApp}=docker-registry.default.svc:5000/jnd-tasks-prod/tasks:${prodTag} -n jnd-tasks-prod"
//        sh "oc delete configmap ${destApp}-config -n jnd-tasks-prod"
//        sh "oc create configmap ${destApp}-config --from-file=./configuration/application-users.properties --from-file=./configuration/application-roles.properties -n jnd-tasks-prod"
//        openshiftDeploy apiURL: '', authToken: '', depCfg: "${destApp}", namespace: 'jnd-tasks-prod', verbose: 'false', waitTime: '', waitUnit: 'sec'
//        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: "${destApp}", namespace: 'jnd-tasks-prod', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '', waitUnit: 'sec'
//
//    }

    stage('Switch over to new Version') {
        echo "Determining active service ..."
        oc = "oc get route tasks -o jsonpath='{ .spec.to.name }' -n jnd-tasks-prod".execute().with{
            def output = new StringWriter()
            def error = new StringWriter()
            it.waitForProcessOutput(output, error)
            println output.toString()
        }
        def ret = sh(script: 'oc get route tasks -o jsonpath=\'{ .spec.to.name }\' -n jnd-tasks-prod', returnStdout: true)
        println ret
        def target = "unknown"

        if (ret.equals("tasks-green"))    {
            target = "tasks-blue"
            println "Cutting over to ${target}"
        }
        else    {
            target = "tasks-green"
            println "Cutting over to ${target}"
        }
        echo "Switching Production application to ${target}."

        sh "oc set image dc/${target} ${target}=docker-registry.default.svc:5000/jnd-tasks-prod/tasks:${prodTag} -n jnd-tasks-prod"
        sh "oc delete configmap ${target}-config -n jnd-tasks-prod"
        sh "oc create configmap ${target}-config --from-file=./configuration/application-users.properties --from-file=./configuration/application-roles.properties -n jnd-tasks-prod"
        openshiftDeploy apiURL: '', authToken: '', depCfg: "${target}", namespace: 'jnd-tasks-prod', verbose: 'false', waitTime: '', waitUnit: 'sec'
        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: "${target}", namespace: 'jnd-tasks-prod', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '', waitUnit: 'sec'
        sleep(10)
        ret = sh(script: "oc patch route/tasks -p '{\"spec\":{\"to\":{\"name\":\"${target}\"}}}' -n jnd-tasks-prod", returnStdout: true)

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