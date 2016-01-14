node {
    
    // Check for prerequisites
    // The assumption is that the Jenkins master will have its JENKINS_HOME
    // directory mounted on a volume hosted on the Docker host.  Share
    // that directory with the build container.
    if (env.JENKINS_CONTAINER_VOLUME == null)
    {
        error "JENKINS_CONTAINER_VOLUME environment needs to be set, in Jenkins master container, with path where shared jenkins_home is mounted on Docker host"
    }

    // Pull down the git repo with the source
    git url: SOURCE_GIT_URL
    
    // Set the volume where jenkins home is mounted on the Docker host.
    // Use this for the job workspace location.
    // NOTE: The JENKINS_CONTAINER_VOLUME environment variable should be set 
    // in the docker run command using the -e parameter.
    def containerVolume = env.JENKINS_CONTAINER_VOLUME
    
    // When specified create the docker run -v parameter to mount maven's 
    // repository cache from the Docker host.
    def mavenCacheVolumeRunParameter = ''
    if (env.MAVEN_CACHE_VOLUME != null)
    {
        mavenCacheVolumeRunParameter = '-v "' + env.MAVEN_CACHE_VOLUME + '":/root/.m2'
    }

    // Get the job's workspace sub-directory minus the JENKINS_HOME directory
    def workspaceSubPath = pwd().substring(env.JENKINS_HOME.length(), pwd().length())
    // Generate the Docker host path where the job's workspace resides
    def hostWorkspaceVolume = containerVolume  + workspaceSubPath
    
    def buildContainerId = 'syndicatebuild' + env.BUILD_ID
    
    echo '***** Start container to run build and unit tests'
    // Start the Maven build container and execute the build.
    // NOTES:
    // Use -t and --entrypoint=cat to keep the container running until withRun
    // ends it.
    // This mounts the Jenkins job workspace on the Docker host into the
    // container.  The build will run from this directory and generate the
    // artifacts here.
    // This mounts the maven repository cache directory that's on the Docker
    // host to store maven dependencies for future build container instances.
    def maven
    maven = docker.image('maven:3.3-jdk-7')
    try
    {
        maven.withRun('-t --entrypoint=cat --name ' + buildContainerId + ' -v "' + hostWorkspaceVolume + '":/usr/build ' + mavenCacheVolumeRunParameter)
        {
            sh 'docker exec -t ' + buildContainerId + ' bash -c "cd /usr/build/demo/repo/javademo && mvn clean package"'
        }

        echo '***** Build and unit tests were successful'
    }
    catch (all)
    {
        error 'Build or unit test failed'
    }

    def appimage
    def testimage
    
    // Build the Docker images for the app and integration test
    parallel "Building Docker app image":
    {
        appimage = docker.build('demoapp:ci','demo/repo/javademo/app')
    },
    "Building Docker integration test image":
    {
        testimage = docker.build('demotest:ci','demo/repo/javademo/integration-test')
    },
    failFast: true
    
    echo '***** Docker builds for images successful'
    
    def appcontainer
    def testcontainer
    echo '***** Integration test stage...Start Docker containers for integration testing'
    try
    {
        appcontainer = appimage.run('-d -i -p 8082:8080 --name demoapp_ci$BUILD_ID')
        testcontainer = testimage.run('-d -i -p 8083:8080 --link demoapp_ci$BUILD_ID:app --name demotest_ci$BUILD_ID')

        echo '***** Preparing for integration testing...Wait for app and test containers to start up'
        retry(10)
        {
            sleep 3
            sh 'docker exec -t demoapp_ci$BUILD_ID curl --write-out "\n" localhost:8080/demoapp/status'
            sh 'docker exec -t demotest_ci$BUILD_ID curl --write-out "\n" localhost:8080/demotest/status'
        }

        echo '***** Execute test on integration test container'
        // This will execute the test from inside the test container.
        // The output will be piped to a file so that it can be read into a
        // variable to process the test output.
        sh 'docker exec -t demotest_ci$BUILD_ID curl --write-out "\n" localhost:8080/demotest/test > test.txt 2>&1'
        def testoutput = readFile('test.txt')
        echo testoutput

        if (testoutput.contains('pass: true'))
        {
            echo '***** Integration test passed.'
        }
        else
        {
            error 'Contract test failed...Aborting build.'
        }
    }
    catch (all)
    {
        // Force build failure.
        // Don't go any futher after cleanup in finally block
        error 'Integration test stage failed'
    }
    finally
    {
        parallel "Stop app container":
        {
            // Executes when user aborts from input.
            // Stops and removes containers
            try
            {
                appcontainer.stop()          
            }
            catch (all)
            {    
            }
        },
        "Stop integration test container":
        {
            try
            {
                testcontainer.stop()          
            }
            catch (all)
            {
            }
        },
        failFast: false
    }
}
