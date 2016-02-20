Appformer package for Jenkins CI
================================
This package deploys Jenkins and the Build Service engines as PODs.
This will create an NFS share that will be mounted inside the PODs.

The steps provided below were based on reading this [wiki link](https://cawiki.ca.com/display/intplatform/HOWTO%3A+Get+Started+With+OSE3#HOWTO:GetStartedWithOSE3-DeployaPlatformApplicationinOSE3).

In the steps, substitute the following values:
- `<ose3project>` - OSE3 Project where Appformer is running.  _(Example: myproject)_
- `<VPC>` - the VPC where the Platform instance is running on.  _(Example: slogvpc4)_

### Testing in personal Appformer environment
You'll need to get the devops team to deploy an Appformer environment for you to use in the Openshift environment.
They'll deploy it to your project.

#### Modify app package engine descriptors for personal testing
Each engine in the app package is a docker image.  For Platform deployment the
package expects the images to be in the platform Openshift project.  However,
to deploy the package in your project, you will need to modify the engine
descriptors to point to your project.

1. Edit `engine/jenkins/descriptor.yml` to replace this line:
```
docker_image: "platform/buildservice-jenkins"
```
with the project where Appformer is running in:
```
docker_image: "<ose3project>/buildservice-jenkins"
```
2. Edit `engine/buildservice/descriptor.yml` to replace this line:
```
docker_image: "platform/buildservice-buildservice"
```
with the project where Appformer is running in:
```
docker_image: "<ose3project>/buildservice-buildservice"
```

#### Push Docker engine images to VDR
Following on the previous section, regarding where the images are located, the Docker
images that the app package refers to needs to be made available in the local
VDR.

1. Build the Jenkins Docker image and push the image
```
cd docker-workflow-plugin/demo
docker build -t ose3vdr1.services.<VPC>.caplatformdev.com:5000/<ose3project>/buildservice-jenkins:1.0.0.0 -f Dockerfile_release .
docker push ose3vdr1.services.<VPC>.caplatformdev.com:5000/<ose3project>/buildservice-jenkins:1.0.0.0
```
2. Build the Build Service Docker image (from the build-service git repo)
```
cd build-service
docker build -t ose3vdr1.services.<VPC>.caplatformdev.com:5000/<ose3project>/buildservice-buildservice:1.0.0.0 .
docker push ose3vdr1.services.<VPC>.caplatformdev.com:5000/<ose3project>/buildservice-buildservice:1.0.0.0
```

#### Create Appformer app
1. Create appformer jenkins app:
```
curl -X POST http://appformer-<ose3project>.app.services.<VPC>.caplatformdev.com/system/appinstance/jenkins
```
Example:
```
curl -X POST http://appformer-myproject.app.services.slogvpc4.caplatformdev.com/system/appinstance/jenkins
```
NOTE: The output will contain a git clone command line that you will use after this.

#### Steps to push app package to Appformer git repo
1. Clone the git repo:
```
git clone http://appformer-<ose3project>.app.services.<VPC>.caplatformdev.com/system/git/jenkins.git
```
Example:
```
git clone http://appformer-myproject.app.services.slogvpc4.caplatformdev.com/system/git/jenkins.git
```
2. Place root of app package directory structure at the root of the cloned git repo and push changes:
```
git add -A
git commit -m "initial"
git push
```

#### Deploy app
1. Use Appformer to deploy the app
```
curl -X POST http://appformer-<ose3project>.app.services.<VPC>.caplatformdev.com/system/appinstance/jenkins/status -d '{"action":"deploy", "project":"<ose3project>"}'  -H 'Content-Type:application/json'
```
Example:
```
curl -X POST http://appformer-myproject.app.services.slogvpc4.caplatformdev.com/system/appinstance/jenkins/status -d '{"action":"deploy", "project":"myproject"}'  -H 'Content-Type:application/json'
```
Once this is complete the PODs will have been deployed and should be running.

#### Verify engines are accessible via service registry routes
1. Obtain the service IP address of the Service Registry on Openshift
```
oc status
``` 
The entry for the Service Registry should look something like this.  The IP address for the service is on svc/serviceregistry line.
```
svc/serviceregistry-1 - 172.30.57.61:80 -> 8080
  dc/serviceregistry-serviceregistry deploys ose3vdr1.services.slogvpc4.caplatformdev.com:5000/platform/serviceregistry-serviceregistry:1.0.2.57 (manual)
    #1 deployed about an hour ago - 1 pod
  exposed by route/serviceregistry-serviceregistry
```
2. Send a request to the Build Service via the Service Registry
```
curl <serviceregistry_ip>/default/ci/buildservice
```
3. Send a request to Jenkins via the Service Registry
```
curl <serviceregistry_ip>/default/ci/jenkins
```


#### Undeploy app
This is needed when you need to push up new changes and to redeploy.
You also need to do this if you decide to delete the app from Appformer.

1. Undeploy app
```
curl -X POST http://appformer-<ose3project>.app.services.<VPC>.caplatformdev.com/system/appinstance/jenkins/status -d '{"action":"undeploy", "project":"<ose3project>"}'  -H 'Content-Type:application/json'
```
Example:
```
curl -X POST http://appformer-myproject.app.services.slogvpc4.caplatformdev.com/system/appinstance/jenkins/status -d '{"action":"undeploy", "project":"myproject"}'  -H 'Content-Type:application/json'
```

#### Delete Appformer app
1. Delete jenkins app:
```
curl -X DELETE http://appformer-<ose3project>.app.services.<VPC>.caplatformdev.com/system/appinstance/jenkins
```
Example:
```
curl -X DELETE http://appformer-myproject.app.services.slogvpc4.caplatformdev.com/system/appinstance/jenkins
```
