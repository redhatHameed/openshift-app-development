#!groovy

node('maven') {

    def org = "mitzicom"
    def app_name = "mlbparks"
    def dev_project = "${org}-dev"
    def prod_project = "${org}-prod"
    def app_url = "http://${app_name}-${dev_project}.apps.ocp.datr.eu"
    def git_url = "https://gogs-cicd.apps.ocp.datr.eu/${org}/${app_name}.git"
    def groupId    = getGroupIdFromPom("pom.xml")
    def artifactId = getArtifactIdFromPom("pom.xml")
    def version    = getVersionFromPom("pom.xml")
    def packaging    = getPackagingFromPom("pom.xml")

    stage('Checkout Source') {
        echo "Git url : $giturl"
        git credentialsId: 'gogs', url: "https://gogs-cicd.apps.ocp.datr.eu/mitzicom/mlbparks.git"
    }


    // Set the tag for the development image: version + build number
    def devTag  = "0.0-0"
    // Set the tag for the production image: version
    def prodTag = "0.0"

    stage('Build war') {
        echo "Building version ${version}"
        sh "mvn -B -s settings.xml clean package -DskipTests"
    }

    // Using Maven run the unit tests
    stage('Unit Tests') {
        echo "Running Unit Tests"
        sh "mvn -B -s settings.xml test"
    }

    // Using Maven call SonarQube for Code Analysis
    stage('Code Analysis') {
        echo "Running Code Analysis"
        sh "mvn -B -s settings.xml sonar:sonar -DskipTests -Dsonar.host.url=https://sonarqube-cicd.apps.ocp.datr.eu"
    }

    // Publish the built war file to Nexus
    stage('Publish to Nexus') {
        echo "Publish to Nexus"
        sh "mvn -B -s settings.xml deploy -DskipTests -DaltDeploymentRepository=nexus::default::https://nexus-cicd.apps.ocp.datr.eu/repository/maven-snapshots"
    }

    //Build the OpenShift Image in OpenShift and tag it.
    stage('Build and Tag OpenShift Image') {
        echo "Building OpenShift container image tasks:${devTag}"
        echo "Project : ${dev_project}"
        echo "App : ${app_name}"
        echo "Group ID : ${groupId}"
        echo "Artifact ID : ${artifactId}"
        echo "Version : ${version}"
        echo "Packaging : ${packaging}"

        sh "mvn -B -s settings.xml dependency:copy -DstripVersion=true -Dartifact=${groupId}:${artifactId}:${version}:${packaging} -DoutputDirectory=."
        sh "cp \$(find . -type f -name \"${artifactId}-*.${packaging}\")  ${artifactId}.${packaging}"
        sh "pwd; ls -ltr"
        sh "oc start-build ${app_name} --follow --from-file=${artifactId}.${packaging} -n ${dev_project}"
        openshiftVerifyBuild apiURL: '', authToken: '', bldCfg: app_name, checkForTriggeredDeployments: 'true', namespace: dev_project, verbose: 'false', waitTime: ''
        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: app_name, destTag: devTag, destinationAuthToken: '', destinationNamespace: dev_project, namespace: dev_project, srcStream: app_name, srcTag: 'latest', verbose: 'false'
    }

    // Deploy the built image to the Development Environment.
    stage('Deploy to Dev') {
        echo "Deploying container image to Development Project"
        echo "Project : ${dev_project}"
        echo "App : ${app_name}"
        echo "Dev Tag : ${devTag}"
        sh "oc set image dc/${app_name} ${app_name}=${dev_project}/${app_name}:${devTag} -n ${dev_project}"
        def ret = sh(script: "oc delete configmap ${app_name}-config --ignore-not-found=true -n ${dev_project}", returnStdout: true)
        ret = sh(script: "oc create configmap ${app_name}-config --from-file=src/main/resources/mlbparks.json -n ${dev_project}", returnStdout: true)
        openshiftDeploy apiURL: '', authToken: '', depCfg: app_name, namespace: dev_project, verbose: 'false', waitTime: '', waitUnit: 'sec'
        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: app_name, namespace: dev_project, replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '', waitUnit: 'sec'
    }

    // Run Integration Tests in the Development Environment.
    stage('Integration Tests') {
        echo "Running Integration Tests"
        //sleep(30)
        openshiftVerifyService apiURL: '', authToken: '', namespace: 'jnd-tasks-dev', svcName: 'tasks', verbose: 'false'
        echo "Checking for app health ..."
        def curlget = "curl -f ${app_url}//ws/healthz".execute().with{
            def output = new StringWriter()
            def error = new StringWriter()
            it.waitForProcessOutput(output, error)
            println it.exitValue()
            assert it.exitValue() == 0: "$error"
        }
        echo "Checking for app info ..."
        curlget = "curl -f ${app_url}//ws/info".execute().with{
            def output = new StringWriter()
            def error = new StringWriter()
            it.waitForProcessOutput(output, error)
            println it.exitValue()
            assert it.exitValue() == 0: "$error"
        }
        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: app_name, destTag: prodTag, destinationAuthToken: '', destinationNamespace: prod_project, namespace: dev_project, srcStream: app_name, srcTag: devTag, verbose: 'false'
    }


    stage('Wait for approval') {

    }

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
//
//    stage('Switch over to new Version') {
//        echo "Determining active service ..."
//        oc = "oc get route tasks -o jsonpath='{ .spec.to.name }' -n jnd-tasks-prod".execute().with{
//            def output = new StringWriter()
//            def error = new StringWriter()
//            it.waitForProcessOutput(output, error)
//            println output.toString()
//        }
//        def ret = sh(script: 'oc get route tasks -o jsonpath=\'{ .spec.to.name }\' -n jnd-tasks-prod', returnStdout: true)
//        println ret
//        def target = "unknown"
//
//        if (ret.equals("tasks-green"))    {
//            target = "tasks-blue"
//            println "Cutting over to ${target}"
//        }
//        else    {
//            target = "tasks-green"
//            println "Cutting over to ${target}"
//        }
//        echo "Switching Production application to ${target}."
//
//        sh "oc set image dc/${target} ${target}=docker-registry.default.svc:5000/jnd-tasks-prod/tasks:${prodTag} -n jnd-tasks-prod"
//        sh "oc delete configmap ${target}-config -n jnd-tasks-prod"
//        sh "oc create configmap ${target}-config --from-file=./configuration/application-users.properties --from-file=./configuration/application-roles.properties -n jnd-tasks-prod"
//        openshiftDeploy apiURL: '', authToken: '', depCfg: "${target}", namespace: 'jnd-tasks-prod', verbose: 'false', waitTime: '', waitUnit: 'sec'
//        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: "${target}", namespace: 'jnd-tasks-prod', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '', waitUnit: 'sec'
//        sleep(10)
//        ret = sh(script: "oc patch route/tasks -p '{\"spec\":{\"to\":{\"name\":\"${target}\"}}}' -n jnd-tasks-prod", returnStdout: true)
//
//    }
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