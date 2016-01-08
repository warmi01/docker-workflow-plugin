# January 2016 POC for demo Java app and tests

#### Proposed Maven structure
Use parent POM as umbrella for separate modules:
1. app module contains the app/unit test source
2. integration-test module contains the integration test source

#### Build artifacts for app and integration test
To compile and create WAR artifacts for all modules:
```
mvn clean package
```
Maven will produce the binaries in a target directory under the
directory for each module.
