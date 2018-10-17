FROM jboss/base-jdk:8

ENV SONAR_VERSION=$SONARQUBE_VERSION \
    SONARQUBE_HOME=/opt/sonarqube \
    SONARQUBE_JDBC_USERNAME=sonar \
    SONARQUBE_JDBC_PASSWORD=sonar \
    SONARQUBE_JDBC_URL=unknown

# Http port
EXPOSE 9000

USER root

RUN groupadd -r sonarqube && useradd -r -g sonarqube sonarqube

## grab gosu for easy step-down from root
#RUN set -x \
#    && wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/1.10/gosu-$(dpkg --print-architecture)" \
#    && wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/1.10/gosu-$(dpkg --print-architecture).asc" \
#    && export GNUPGHOME="$(mktemp -d)" \
#    && gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
#    && gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
#    && rm -rf "$GNUPGHOME" /usr/local/bin/gosu.asc \
#    && chmod +x /usr/local/bin/gosu \
#    && gosu nobody true

RUN set -x \
    && cd /opt \
    && curl -o sonarqube.zip -fSL https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-$SONAR_VERSION.zip \
    && unzip sonarqube.zip \
    && mv sonarqube-$SONAR_VERSION sonarqube \
    && chown -R sonarqube:sonarqube sonarqube \
    && rm sonarqube.zip* \
    && touch /opt/sonarqube/temp/README.txt \
    && rm -rf $SONARQUBE_HOME/bin/*

VOLUME "$SONARQUBE_HOME/data"
VOLUME "$SONARQUBE_HOME/extensions"

COPY run.sh $SONARQUBE_HOME/bin/
RUN chown -R sonarqube:sonarqube $SONARQUBE_HOME && chmod -R 777 $SONARQUBE_HOME
USER sonarqube
WORKDIR $SONARQUBE_HOME
ENTRYPOINT ["./bin/run.sh"]