node {
    
    prereqCheck()
    
    // Pull down the git repo with the source
    checkout scm
    
    // Run the build and unit tests inside a maven container
    buildWithMavenContainer('/demo/repo/javademo')
    
    // Build the Docker images for the app and integration test
    // from the artifacts built in the workspace
    def images = buildDockerImages()
    
    // Start Docker app/test containers for integration testing
    runIntegrationTests(images)
}

def prereqCheck()
{
    if (env.JENKINS_CONTAINER_NAME == null)
    {
        error "JENKINS_CONTAINER_NAME environment variable needs to be set in docker run command with -e to specify container name used in --name."
    }
}

// Run maven build that will compile, run unit tests, and create artifact binaries
def buildWithMavenContainer(projectDirectory)
{
    def buildContainerId = 'syndicatebuild' + env.BUILD_ID
    def buildPath = pwd() + projectDirectory
    
    echo '***** Start container to run build and unit tests'
    def maven
    maven = docker.image('maven:3.3-jdk-7')
    try
    {    
        // Start the Maven build container and execute the build.
        // NOTES:
        // This mounts /var/jenkins_home from the Jenkins container (using --volumes-from)
        // and sets the current work directory (-w) to the workspace directory.
        maven.withRun('--name ' + buildContainerId +
                      ' --volumes-from=' + env.JENKINS_CONTAINER_NAME +
                      ' -w ' + buildPath +
                      ' -t --entrypoint=cat')
        {
            // Store the maven cache repository under the jenkins_home directory.
            // Create the maven user settings to specify the repository location. 
            sh 'docker exec -t ' + buildContainerId + ' bash -c "mkdir -p $HOME/.m2"'
            sh 'docker exec -t ' + buildContainerId + ' bash -c "echo \'<settings><localRepository>' + env.JENKINS_HOME + '/.m2/repository</localRepository></settings>\' > $HOME/.m2/settings.xml"'
            
            // Run build from the mounted volume relative to the
            // job's workspace directory.
            sh 'docker exec -t ' + buildContainerId + ' bash -c "mvn clean package"'
        }

        echo '***** Build and unit tests were successful'
    }
    catch (all)
    {
        error 'Build or unit test failed'
    }    
}

def buildDockerImages()
{
    def appimage
    def testimage
    
    parallel "Building Docker app image":
    {
        appimage = docker.build('demoapp:ci','demo/repo/javademo/app')
    },
    "Building Docker integration test image":
    {
        testimage = docker.build('demotest:ci','demo/repo/javademo/integration-test')
    },
    failFast: false
    
    echo '***** Docker builds for images successful'
    
    return [appimage, testimage]
}

def runIntegrationTests(images)
{
    def appimage = images[0]
    def testimage = images[1]
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
            sleep 3L
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
            error 'Integration test stage failed'
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