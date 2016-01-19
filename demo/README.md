Docker image for Docker workflow demo
=====================================
This image contains a "Docker Workflow" Job that demonstrates Jenkins Workflow integration
with Docker via [CloudBees Docker Workflow](https://wiki.jenkins-ci.org/display/JENKINS/CloudBees+Docker+Workflow+Plugin) plugin.

#### Build Docker image
```
make build
```

#### Run Docker image
NOTE: the original command string below has been modifed to include '-v /var/run/docker.sock:/var/run/docker.sock', which is important for allowing the inner docker commands within Jenkins workflow to run in the outer docker that Jenkins runs in (i.e. shared docker images/containers, ports accessible to host, etc.)

**_(December 2015)_**
```
docker run -d -v /var/run/docker.sock:/var/run/docker.sock -p 8080:8080 -p 8081:8081 -p 8022:22 --add-host=docker.example.com:127.0.0.1 -ti --privileged jenkinsci/docker-workflow-demo:1.2

```

**_(January 2016)_**

Jenkins master container will mount /var/jenkins_home on the Docker host's /var/jenkins_home directory
that will contain all JENKINS_HOME data.
This is a step towards persisting container data, however the main goal here is to allow the build container
to mount a volume to the workspace directory where source will be built from.
```
docker run -d -e MAVEN_CACHE_VOLUME=<shared maven cache directory> -v /var/run/docker.sock:/var/run/docker.sock -p 8080:8080 -p 8081:8081 -p 8022:22 --add-host=docker.example.com:127.0.0.1 -ti --privileged -v /var/jenkins_home:/var/jenkins_home jenkinsci/docker-workflow-demo:1.0
```
NOTE:

Substitute `<shared maven cache directory>` with the directory path where the maven repository cache will reside on the Docker host.
*-e MAVEN_CACHE_VOLUME* will set this environment variable value inside the Jenkins container.  The workflow script
will use the value to mount the volume in the build container.

#### Accessing Jenkins web console
To access the Jenkins web console from outside the Docker environment, add the following line to your SSH config for the Docker host.
```
LocalForward localhost:8080 localhost:8080
```

#### Additional notes
The "Docker Workflow" Job simply does the following:

1. Gets the Spring Pet Clinic demonstration application code from GitHub.
1. Builds the Pet Clinic application in a Docker container.
1. Builds a runnable Pet Clinic application Docker image.
1. Runs a Pet Clinic app container (from the Pet Clinic application Docker image) + a second maven3 container that runs automated tests against the Pet Clinic app container.
  * The 2 containers are linked, allowing the test container to fire requests at the Pet Clinic app container.

The "Docker Workflow" Job demonstrates how to use the `docker` DSL:

1. Use `docker.image` to define a DSL `Image` object (not to be confused with `build`) that can then be used to perform operations on a Docker image:
  * use `Image.inside` to run a Docker container and execute commands in it. The build workspace is mounted as the working directory in the container.
  * use `Image.run` to run a Docker container in detached mode, returning a DSL `Container` object that can be later used to stop the container (via `Container.stop`).
1. Use `docker.build` to build a Docker image from a `Dockerfile`, returning a DSL `Image` object that can then be used to perform operations on that image (as above). 
  
The `docker` DSL supports some additional capabilities not shown in the "Docker Workflow" Job:
  
1. Use the `docker.withRegistry` and `docker.withServer` to register endpoints for the Docker registry and host to be used when executing docker commands.
  * `docker.withRegistry(<registryUrl>, <registryCredentialsId>)`
  * `docker.withServer(<serverUri>, <serverCredentialsId>)` 
1. Use the `Image.pull` to pull Docker image layers into the Docker host cache.
1. Use the `Image.push` to push a Docker image to the associated Docker Registry. See `docker.withRegistry` above. 
