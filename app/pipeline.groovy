#!groovy

node('maven') {

    stage('Checkout Source') {
        git credentialsId: 'gogs', url: "${git_url}"
    }

    def dev_project = "${org}-dev"
    def prod_project = "${org}-prod"
    def app_url_dev = "http://${app_name}-${dev_project}.apps.ocp.datr.eu"
    def app_url = "http://${app_name}.apps.ocp.datr.eu"
    def groupId    = getGroupIdFromPom("pom.xml")
    def artifactId = getArtifactIdFromPom("pom.xml")
    def version    = getVersionFromPom("pom.xml")
    def packaging    = getPackagingFromPom("pom.xml")

    stage('Build war') {
        echo "Building version ${version}"
        sh "mvn -U -q -s settings.xml clean package -DskipTests"
    }

    // Using Maven run the unit tests
    stage('Unit Tests') {
        echo "Running Unit Tests"
        sh "mvn -q -s settings.xml test"
    }

    // Using Maven call SonarQube for Code Analysis
    stage('Code Analysis') {
        echo "Running Code Analysis"
        sh "mvn -q -s settings.xml sonar:sonar -DskipTests -Dsonar.host.url=https://sonarqube-cicd.apps.ocp.datr.eu"
    }

    // Publish the built war file to Nexus
    stage('Publish to Nexus') {
        echo "Publish to Nexus"
        sh "mvn -q -s settings.xml deploy -DskipTests -DaltDeploymentRepository=nexus::default::https://nexus-cicd.apps.ocp.datr.eu/repository/maven-snapshots"
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

        sh "mvn -q -s settings.xml dependency:copy -DstripVersion=true -Dartifact=${groupId}:${artifactId}:${version}:${packaging} -DoutputDirectory=."
        sh "cp \$(find . -type f -name \"${artifactId}-*.${packaging}\")  ${artifactId}.${packaging}"
        sh "pwd; ls -ltr"
        sh "oc rollout pause dc/${app_name} -n ${dev_project}"
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
        ret = sh(script: "oc create configmap ${app_name}-config --from-file=${config_file} -n ${dev_project}", returnStdout: true)
        sh "oc rollout resume dc/${app_name} -n ${dev_project}"
        openshiftDeploy apiURL: '', authToken: '', depCfg: app_name, namespace: dev_project, verbose: 'false', waitTime: '180', waitUnit: 'sec'
        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: app_name, namespace: dev_project, replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '180', waitUnit: 'sec'
    }

    // Run Integration Tests in the Development Environment.
    stage('Integration Tests') {
        echo "Running Integration Tests"

        openshiftVerifyService apiURL: '', authToken: '', namespace: dev_project, svcName: app_name, verbose: 'false'
        echo "Checking for app health ..."
        def curlget = "curl -f ${app_url_dev}/ws/healthz".execute().with{
            def output = new StringWriter()
            def error = new StringWriter()
            it.waitForProcessOutput(output, error)
            assert it.exitValue() == 0: "$error"
        }

        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: app_name, destTag: prodTag, destinationAuthToken: '', destinationNamespace: dev_project, namespace: dev_project, srcStream: app_name, srcTag: devTag, verbose: 'false'
    }


    stage('Wait for approval to be staged in production') {
        timeout(time: 2, unit: 'DAYS') {
            input message: 'Approve this build to be staged in production ?'
        }
    }

    // Blue/Green Deployment into Production
    def destApp   = "${app_name}-green"
    def activeApp = ""

    stage("Deploying ${app_name} into Production") {
        echo "Determining currently active service ..."

        def active_service = sh(script: "oc get route ${app_name} -o jsonpath=\'{ .spec.to.name }\' -n ${prod_project}", returnStdout: true)
        println "${active_service} is the currently active service"

        def target = "unknown"
        if (active_service.equals(app_name + "-green")) {
            target = app_name+"-blue"
        } else {
            target = app_name+"-green"
        }
        println "So staging ${app_name} to ${target}"

        sh "oc set image dc/${target} ${target}=${dev_project}/${app_name}:${prodTag} -n ${prod_project}"
        def ret = sh(script: "oc delete configmap ${target}-config --ignore-not-found=true -n ${prod_project}", returnStdout: true)
        ret = sh(script: "oc create configmap ${target}-config --from-file=${config_file} -n ${prod_project}", returnStdout: true)
        openshiftDeploy apiURL: '', authToken: '', depCfg: target, namespace: prod_project, verbose: 'false', waitTime: '180', waitUnit: 'sec'
        openshiftVerifyDeployment apiURL: '', authToken: '', depCfg: target, namespace: prod_project, replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '180', waitUnit: 'sec'
        openshiftVerifyService apiURL: '', authToken: '', namespace: prod_project, svcName: target, verbose: 'false'

        echo "Checking ${target} app health ..."
        def curlget = "curl -f http://${target}-${prod_project}.apps.ocp.datr.eu/ws/healthz".execute().with {
            def output = new StringWriter()
            def error = new StringWriter()
            it.waitForProcessOutput(output, error)
            assert it.exitValue() == 0: "$error"
        }
        echo "App health looks good !"
    }

    stage('GO LIVE !!!!!') {

        def active_service = sh(script: "oc get route ${app_name} -o jsonpath=\'{ .spec.to.name }\' -n ${prod_project}", returnStdout: true)
        println "${active_service} is the currently active service"

        def target = "unknown"
        if (active_service.equals(app_name + "-green")) {
            target = app_name+"-blue"
        } else {
            target = app_name+"-green"
        }
        timeout(time: 2, unit: 'DAYS') {
            input message: "Approve ${target} to GO LIVE ?"
        }

        //Finally cut over the route
        ret = sh(script: "oc patch route/${app_name} -p '{\"spec\":{\"to\":{\"name\":\"${target}\"}}}' -n ${prod_project}", returnStdout: true)
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