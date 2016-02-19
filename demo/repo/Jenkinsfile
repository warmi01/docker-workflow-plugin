node {
    
     def images = ['app': null, 'app_tests': null]
     def containers = ['app': null, 'app_tests': null]

    // Pull down the git repo with the source
    checkout scm
    
    docker.withServer(env.CI_DOCKER_HOST)
    {
        try
        {
            def version = docker.script.readFile('demo/repo/version.txt').trim() 
            def imagetag = "${version}.${env.BUILD_ID}"
    
            // Run the build and unit tests inside a maven container
            stage 'build java artifacts'
            buildWithMavenContainer('demo/repo/javademo', imagetag)
            
            // Build the Docker images for the app and integration test
            // from the artifacts built in the workspace
            stage 'build docker images'
            buildDockerImages('demo/repo/javademo', images, imagetag)
            
            // Start Docker app/test containers for integration testing
            stage 'run integration tests'
            runIntegrationTests(containers, images, imagetag)        
    
            // Publish the Docker images to a Docker registry
            stage 'push docker images'
            publishDockerImages(images, imagetag)
        }
        catch (all)
        {
            error 'Pipeline error'
        }
        finally
        {
            cleanup(images, containers)
        }    
    }
}

// Run maven build that will compile, run unit tests, and create artifact binaries
def buildWithMavenContainer(projectDirectory, imagetag)
{
    def buildContainerId = 'syndicatebuild-' + imagetag
    def buildPath = pwd() + '/' + projectDirectory
    
    echo '***** Start container to run build and unit tests'
    def maven = docker.image('maven:3.3-jdk-7')
    
    try
    {    
        // Start the Maven build container
        // NOTES:
        // This mounts a volume (with -v), inside the Maven container, 
        // containing the project source directory to build from.
        // -w sets the current work directory (-w) to the project source
        // directory.
        // The maven cache volume is stored on the Docker host and is mapped
        // into this container.
        maven.withRun('--name ' + buildContainerId +
                    ' -v ' + buildPath + ':' + buildPath +
                    ' -v /m2repo:/m2repo ' +
                    ' -w ' + buildPath +
                    ' -t --entrypoint=cat')
        {                
            // Execute the Maven build inside the container.
            // The package directive compiles the source, runs the unit test,
            // creates the artifacts.
            docker.script.sh 'docker exec -t ' + buildContainerId + ' bash -c "mvn -Dmaven.repo.local=/m2repo clean package"'
        }

        echo '***** Build and unit tests were successful'
    }
    catch (all)
    {
        error 'Build or unit test failed'
    }    
}

def buildDockerImages(projectDirectory, images, imagetag)
{
    parallel "Building Docker app image":
    {
        images.app = docker.build("${env.JOB_NAME}:${imagetag}", "${projectDirectory}/app")
    },
    "Building Docker integration test image":
    {
        images.app_tests = docker.build("${env.JOB_NAME}-tests:${imagetag}", "${projectDirectory}/integration-test")
    },
    failFast: false
    
    echo '***** Docker builds for images successful'
}

def runIntegrationTests(containers, images, imagetag)
{
    echo '***** Integration test stage...Start Docker containers for integration testing'
    try
    {
        containers.app = images.app.run("-d -i --name ${env.JOB_NAME}-${imagetag}")
        containers.app_tests = images.app_tests.run("-d -i --link ${env.JOB_NAME}-${imagetag}:app --name ${env.JOB_NAME}-tests-${imagetag}")

        echo '***** Preparing for integration testing...Wait for app and test containers to start up'
        retry(10)
        {
            sleep 3L
            docker.script.sh "docker exec -t ${env.JOB_NAME}-${imagetag} curl --write-out '\n' localhost:8080/demoapp/status"
            docker.script.sh "docker exec -t ${env.JOB_NAME}-tests-${imagetag} curl --write-out '\n' localhost:8080/demotest/status"
        }

        echo '***** Execute test on integration test container'
        // This will execute the test from inside the test container.
        // The output will be piped to a file so that it can be read into a
        // variable to process the test output.
        docker.script.sh "docker exec -t ${env.JOB_NAME}-tests-${imagetag} curl --write-out '\n' localhost:8080/demotest/test > test.txt 2>&1"
        def testoutput = docker.script.readFile('test.txt')
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
}

def cleanup(images, containers) {

     parallel "Stop demo app container":
     {
          try {
               containers.app.stop()
               docker.script.sh "docker rmi ${images.app.id}"
          }
          catch (all) {echo 'Error stopping demo app container'}
     },
     "Stop integration tests container":
     {
          try {
               containers.app_tests.stop()
               docker.script.sh "docker rmi ${images.app_tests.id}"
          }
          catch (all) {echo 'Error stopping integration tests container'}
     },
     failFast: false
}

def publishDockerImages(images, imagetag) {

     try {
          // temporariy use fully qualified VDR name; shorter ose3vdr1 should be used once devops docker changes made
          docker.withRegistry('http://ose3vdr1.services.slogvpc4.caplatformdev.com:5000', 'docker-registry-login') {
               
               images.app.push(imagetag)
               images.app_tests.push(imagetag)
          }
     }
     catch (all) {
          echo 'Failed to tag/push to VDR image'
          error 'Failed to tag/push to VDR image'
     }
}

