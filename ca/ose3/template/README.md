Jenkins and Build Service OSE3 template
=======================================

POC Openshift 3 template for a Jenkins server and Build Service.
Creates a POD for the Jenkins server and a POD for the Build Service.
Each POD gets its own Service to expose network access into the POD.
Openshift injects environment variables, with the host/port for all Services,
inside each POD.  This simplifies POD to POD communication.
An NFS share is mounted inside the Jenkins POD to store JENKINS_HOME data.

## Create Openshift Image Streams
Openshift needs image streams to pull Docker images from.
The assumption is that the Jenkins and Build Service Docker images have already been built.

#### Jenkins Image Stream 
```
docker tag <Jenkins Docker image> ose3vdr1.services.slogvpc4.caplatformdev.com:5000/<Openshift project>/jenkins-syndicate:latest

docker push ose3vdr1.services.slogvpc4.caplatformdev.com:5000/<Openshift project>/jenkins-syndicate:latest

oc tag ose3vdr1.services.slogvpc4.caplatformdev.com:5000/<Openshift project>/jenkins-syndicate:latest <Openshift project>/jenkins-syndicate:latest

oc import-image jenkins-syndicate --insecure-repository true
```

#### Build Service Image Stream 
```
docker tag <Build Service Docker image> ose3vdr1.services.slogvpc4.caplatformdev.com:5000/<Openshift project>/build-service:latest

docker push ose3vdr1.services.slogvpc4.caplatformdev.com:5000/<Openshift project>/build-service:latest

oc tag ose3vdr1.services.slogvpc4.caplatformdev.com:5000/<Openshift project>/build-service:latest <Openshift project>/build-service:latest

oc import-image build-service --insecure-repository true
```

## Deploy Openshift App
```
oc new-app -f jenkins-syndicate.json
```
NOTE:
To customize the deployment you should specify the following parameters:

JENKINS_HOME NFS server and path
```
--param=NFS_SERVER=<NFS servername>,NFS_SERVER_PATH=<NFS path>
```

Remote Docker host/port
```
--param=CI_DOCKER_HOST=<host>:<port>
```

See the [wiki page](https://cawiki.ca.com/display/intplatform/Install+Jenkins+on+OSE3) for additional information (i.e., for NFS storage).
