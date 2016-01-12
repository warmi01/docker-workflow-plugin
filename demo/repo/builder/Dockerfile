FROM maven:3.3.3-jdk-8

# install go
RUN wget https://storage.googleapis.com/golang/go1.4.1.linux-amd64.tar.gz
RUN tar -C /usr/local -xzf go1.4.1.linux-amd64.tar.gz
RUN mkdir $HOME/go
ENV GOPATH=$HOME/go
ENV PATH=$PATH:/usr/local/go/bin

# install s2i
RUN go get github.com/openshift/source-to-image
RUN cd ${GOPATH}/src/github.com/openshift/source-to-image
ENV PATH=$PATH:${GOPATH}/src/github.com/openshift/source-to-image/_output/local/bin/linux/amd64/
RUN hack/build-go.sh
