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
docker run --privileged -p 32375:2375 -e JENKINS_HOME_NFS_PATH=<NFS path> -d dind-syndicate:1.10 --insecure-registry ose3vdr1:5000
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
is using.  If Jenkins was deployed using Appformer and you need to determine the NFS path, you have to open a shell
on the Jenkins container and run the mount command to list all the mount points.  Look for jenkins_home in
the mount point name.  Here's an example:
```
/ # mount
10.0.3.100:/export/dss_nfs/quaha01/07nPUp2PFqRbobSk3amGfk/jenkins_07npup2pfqrbobsk3amgfk on /var/jenkins_home_ca type nfs4 (rw,relatime,vers=4.0,rsize=524288,wsize=524288,namlen=255,hard,proto=tcp,port=0,timeo=600,retrans=2,sec=sys,clientaddr=10.1.4.10,local_lock=none,addr=10.0.3.100)
```

Make sure to specify the IP address of NFS server since the distribution of Linux (Alpine) used
by DIND doesn't resolve host names properly (https://github.com/gliderlabs/docker-alpine/issues/8). 
For example:
```
-e JENKINS_HOME_NFS_PATH=10.0.3.100:/export/dss_nfs/quaha01/07nPUp2PFqRbobSk3amGfk/jenkins_07npup2pfqrbobsk3amgfk
```
Here's a [wiki link](https://cawiki.ca.com/display/intplatform/Install+Jenkins+on+OSE3#InstallJenkinsonOSE3-AllocateNFSStorage) on how we're getting NFS storage from DSS for Jenkins.
This only applies when not using Appformer to deploy Jenkins.

NOTE:
To be able to push images to an external docker registry, the Docker daemon needs to know about it.  In our case we
use an insecure registry, ose3vdr1:5000.  The following option needs to be passed to specify the registry when starting the container:
```
--insecure-registry ose3vdr1:5000
```

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
docker -H ose3master1:32375 images
```
