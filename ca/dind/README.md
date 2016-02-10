Using Docker in Docker
======================

To run our own Docker environment, one way is to use a Docker
in Docker (DIND) approach.  The concept here is that a Docker daemon runs
inside of a Docker container (hosted on the Openshift/Docker host).
This Docker daemon will host its own Docker environment and manage its
own containers.  The daemon supports remote API requests.

#### Build Docker image for DIND
Build the DIND Docker image on the Openshift Docker host.
```
docker build -t dind-syndicate:1.10 .
```

#### Start DIND Container
Start the container on the Openshift Docker host.
```
docker run --privileged -p 32375:2375 -e JENKINS_HOME_NFS_PATH=<NFS path> -d dind-syndicate:1.10
```
NOTE:
The Docker daemon listens to remote requests on port 2375.  To avoid conflicts with the host
Docker daemon, we have to expose a different port for the DIND container.
In this example we're forwarding port 32375 (on Openshift Docker host) to port 2375 inside
the container.   
NOTE:
The container will _optionally_ mount an NFS volume under /var/jenkins_home intended to contain
JENKINS_HOME data for a Jenkins server.  To enable this, you must set the
`-e JENKINS_HOME_NFS_PATH=<NFS path>` parameter.  Substitute `<NFS path>` with an NFS path that Jenkins
is using.
For example:
```
-e JENKINS_HOME_NFS_PATH=10.0.3.100:/export/pathdss_nfs/jenkins-syndicate/jenkins-syndicate/omPvvgtBDM
```
Here's a [wiki link](https://cawiki.ca.com/display/intplatform/Install+Jenkins+on+OSE3#InstallJenkinsonOSE3-AllocateNFSStorage) on how we're getting NFS storage from DSS for Jenkins.

#### Getting Shell Access to DIND Container
The container is running Alpine Linux and uses an ash shell (not bash).
```
docker exec -ti <container name> ash
```

#### Access Remote Docker Daemon from CLI
The Docker CLI supports sending requests to a remote Docker daemon.  All CLI commands must start
with `-H <Docker host>' followed by the usual syntax.
For example:
```
docker -H ose3master1.services.slogvpc4.caplatformdev.com:32375 images
```
