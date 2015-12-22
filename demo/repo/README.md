### December 2015 Phase 1 POC
Embed the **SyndicateFlow.groovy** script text in the existing **demoapp-workflow** workflow job configuration.

To be able to access the demoapp/demotest containers from outside the Docker environment, add the following to your SSH config for the Docker host.
```
LocalForward localhost:8082 localhost:8082
LocalForward localhost:8083 localhost:8083
LocalForward localhost:8084 localhost:8084
LocalForward localhost:8085 localhost:8085
```
