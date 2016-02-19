Appformer package for Jenkins CI
================================
This package deploys Jenkins and the Build Service engines.
This will create an NFS share that will be mounted inside the PODs.

The steps provided below were based on reading this [wiki link](https://cawiki.ca.com/display/intplatform/HOWTO%3A+Get+Started+With+OSE3#HOWTO:GetStartedWithOSE3-DeployaPlatformApplicationinOSE3).

In the steps, substitute the following values:
- `<ose3project>` - OSE3 Project where Appformer is running.  _(Example: myproject)_
- `<VPC>` - the VPC where the Platform instance is running on.  _(Example: slogvpc4)_

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
