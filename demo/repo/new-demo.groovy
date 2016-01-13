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
    }
    catch (all)
    {
        error 'Build or unit test failed'
    }

}
