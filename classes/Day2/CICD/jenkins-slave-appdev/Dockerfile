FROM openshift/jenkins-slave-maven-centos7:v3.7

MAINTAINER Justin Davis <justinndavis@gmail.com>

USER root

RUN yum install -y skopeo && \
    yum clean all -y && \
    rm -rf /var/cache/yum

USER 1001