FROM jenkinsci/workflow-demo:1.8

###############################################################
# Docker in Docker https://github.com/jpetazzo/dind 
    
# Install Docker from Docker Inc. repositories.
# RUN curl -sSL https://get.docker.com/ubuntu/ | sh
# RUN curl -sSL https://get.docker.com/ | sh
# Ensure docker client/server versions match (1.8.2)
COPY docker.list /etc/apt/sources.list.d/docker.list
RUN apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
RUN apt-get update
RUN apt-get install -y -q docker-engine=1.8.2-0~jessie

# Install the magic wrapper.
ADD wrapdocker /usr/local/bin/wrapdocker
RUN chmod +x /usr/local/bin/wrapdocker

# Define additional metadata for our image.
VOLUME /var/lib/docker

#
###############################################################

RUN apt-get install -y apparmor apache2-utils

COPY workflow-reg-proxy.conf /tmp/files/regup/workflow-reg-proxy.conf
COPY gen-security-data.sh /usr/local/bin/gen-security-data.sh
RUN /usr/local/bin/gen-security-data.sh /tmp/files/regup/sec
COPY run-demo.sh /usr/local/bin/run-demo.sh

COPY plugins.txt docker-workflow-version.txt /tmp/files/
RUN sed -i "s/@VERSION@/`cat /tmp/files/docker-workflow-version.txt`/g" /tmp/files/plugins.txt
RUN /usr/local/bin/plugins.sh /tmp/files/plugins.txt
RUN touch /usr/share/jenkins/ref/plugins/credentials.jpi.pinned

ADD JENKINS_HOME /usr/share/jenkins/ref

ADD repo /tmp/repo
RUN git config --global user.email "demo@jenkins-ci.org" && git config --global user.name "Docker Workflow Demo" && cd /tmp/repo && git init && git add . && git commit -m 'demo'

# wrapdocker has been modified to launch Jenkins via the installed run.sh script
CMD ["wrapdocker"]
