FROM maven:3.3.3-jdk-8

# install docker
# Ensure docker client/server versions match (1.8.2)
COPY ../../docker.list /etc/apt/sources.list.d/docker.list
RUN apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
RUN apt-get update
RUN apt-get install -y -q docker-engine=1.8.2-0~jessie

# Install the magic wrapper.
ADD ../..wrapdocker /usr/local/bin/wrapdocker
RUN chmod +x /usr/local/bin/wrapdocker

# Define additional metadata for our image.
VOLUME /var/lib/docker

# install go
RUN wget https://storage.googleapis.com/golang/go1.4.1.linux-amd64.tar.gz; tar -C /usr/local -xzf go1.4.1.linux-amd64.tar.gz
RUN mkdir $HOME/go
ENV GOPATH=$HOME/go
ENV PATH=$PATH:/usr/local/go/bin

# install s2i
RUN go get github.com/openshift/source-to-image
ENV PATH=$PATH:${GOPATH}/src/github.com/openshift/source-to-image/_output/local/bin/linux/amd64/
RUN cd ${GOPATH}/src/github.com/openshift/source-to-image; hack/build-go.sh

CMD echo "maven builder running"
