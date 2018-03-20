FROM openshift/jenkins-slave-maven-centos7:v3.7

MAINTAINER Justin Davis <justinndavis@gmail.com>

USER root

RUN yum install -y skopeo && \
    yum clean all -y && \
    rm -rf /var/cache/yum

RUN mv /home/jenkins/.m2/settings.xml /home/jenkins/.m2/settings.original

COPY settings.xml /home/jenkins/.m2/settings.xml

RUN ls -tr /home/jenkins/.m2 && cat /home/jenkins/.m2/settings.xml

USER 1001