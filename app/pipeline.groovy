#!groovy

node('maven-appdev') {

    stage('Checkout Source') {
        git credentialsId: 'gogs', url: "${git_url}"
    }

    def approval_required = true

    def dev_project  = "${org}-dev"
    def prod_project = "${org}-prod"
    def app_url_dev  = "http://${app_name}.${dev_project}.svc:8080"
    def groupId      = getGroupIdFromPom("pom.xml")
    def artifactId   = getArtifactIdFromPom("pom.xml")
    def version      = getVersionFromPom("pom.xml")
    def packaging    = getPackagingFromPom("pom.xml")
    def sonar_url    = "http://sonarqube.cicd.svc:9000"
    def nexus_url    = "http://nexus.cicd.svc:8081/repository/maven-snapshots"


    stage('Build war') {
        echo "Building version ${version}"
        sh "mvn -U -q -s settings.xml clean package -DskipTests"
    }

    // Using Maven run the unit tests
    stage('Unit Tests') {
        echo "Running Unit Tests"
        sh "mvn -B -s settings.xml test"
    }

    // Using Maven call SonarQube for Code Analysis
    stage('Code Analysis') {
        echo "Running Code Analysis"
        sh "mvn -q -s settings.xml sonar:sonar -DskipTests -Dsonar.host.url=${sonar_url}"
    }

    // Publish the built war file to Nexus
    stage('Publish to Nexus') {
        echo "Publish to Nexus"
        sh "mvn -q -s settings.xml deploy -DskipTests -DaltDeploymentRepository=nexus::default::${nexus_url}"
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

        if (app_name.equals("nationalparks") || app_name.equals("mlbparks"))    {
            echo "Performing Integration Tests ..."
            curlget = "curl -f ${app_url_dev}/ws/data/load".execute().with{
                def output = new StringWriter()
                def error = new StringWriter()
                it.waitForProcessOutput(output, error)
                assert it.exitValue() == 0: "$error"
            }
            curlget = "curl -f ${app_url_dev}/ws/data/all".execute().with{
                def output = new StringWriter()
                def error = new StringWriter()
                it.waitForProcessOutput(output, error)
                assert it.exitValue() == 0: "$error"
            }
        }

        openshiftTag alias: 'false', apiURL: '', authToken: '', destStream: app_name, destTag: prodTag, destinationAuthToken: '', destinationNamespace: dev_project, namespace: dev_project, srcStream: app_name, srcTag: devTag, verbose: 'false'
    }

    stage('Pushing to Nexus Docker Registry Using Skopeo') {
//        sh"skopeo \\\n" +
//                "    --insecure-policy \\\n" +
//                "    copy \\\n" +
//                "    --src-creds=justin:\$(oc whoami -t) \\\n" +
//                "    --dest-creds=admin:admin123 \\\n" +
//                "    --src-tls-verify=false \\\n" +
//                "    --dest-tls-verify=false \\\n" +
//                "    docker://docker-registry.default.svc:5000/${dev_project}/${app_name}:${devTag} \\\n" +
//                "    docker://registry-jnd-nexus.apps.fra.example.opentlc.com/jnd-jenkins/jenkins-slave-maven-jnd:latest"
    }

    stage('Wait for approval to be staged in production') {
        if (approval_required) {
            timeout(time: 2, unit: 'DAYS') {
                input message: 'Approve this build to be staged in production ?'
            }
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

        def curlget = "curl -f http://${target}.${prod_project}.svc:8080/ws/healthz".execute().with {
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
            origin = app_name+"-green"
            target = app_name+"-blue"
        } else {
            origin = app_name+"-blue"
            target = app_name+"-green"
        }

        if (approval_required) {
            timeout(time: 2, unit: 'DAYS') {
                input message: "Approve ${target} to GO LIVE ?"
            }
        }

        if (!app_name.equals("parksmap")) {
            ret = sh(script: "oc label --overwrite service ${target} type=parksmap-backend -n ${prod_project}", returnStdout: true)
            println ret
            ret = sh(script: "oc label --overwrite service ${origin} type=silent -n ${prod_project}", returnStdout: true)
            println ret
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