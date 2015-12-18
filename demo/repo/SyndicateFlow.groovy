// Test to see Jenkins workflow build parameter has been defined
try
{
   APP_VERSION
}
catch (all)
{
   error 'APP_VERSION build parameter must be defined in Jenkins workflow configuration'
}

node
{
   git '/tmp/repo'

   stage '***** Integration Stage'
   
   // Get app version from Jenkins build parameter
   sh 'cp demoapp/Docker_v' + APP_VERSION + ' demoapp/Dockerfile'
   sh 'cp demotest/Docker_Integration demotest/Dockerfile'

   def appimage
   def testimage
   def appcontainer
   def testcontainer

   echo '***** Build app and test Docker images'
   parallel "Building Docker app image":
   {
      appimage = docker.build('demoapp:integration','demoapp')
   },
   "Building Docker test image":
   {
      testimage = docker.build('demotest:integration','demotest')
   },
   failFast: true
   
   echo '***** Run integration Docker Images'
   try
   {
      appcontainer = appimage.run('-d -i -p 8082:8080 --name demoapp_integration$BUILD_ID')
      testcontainer = testimage.run('-d -i -p 8083:8080 --link demoapp_integration$BUILD_ID:app --name demotest_integration$BUILD_ID')

      echo '***** Preparing for testing...Wait for app and test containers to start up'
      retry(10)
      {
         sleep 3
         sh 'docker exec -t demoapp_integration$BUILD_ID curl --silent --show-error localhost:8080'
         sh 'docker exec -t demotest_integration$BUILD_ID curl --silent --show-error localhost:8080'
      }

      echo '***** Execute test on test container'
      // This will output results to console output
      sh 'docker exec -t demotest_integration$BUILD_ID curl --write-out "\n" localhost:8080/test'

      input "Integration image ok?"

      echo '***** Image verified...Tagging images for preproduction'
      appimage.tag('preprod', true)
      testimage.tag('preprod', true)
   }
   catch(all)
   {
      // Force build failure.
      // Don't go any futher after cleanup in finally block
      error 'Integration stage failed'
   }
   finally
   {
      // Executes when user aborts from input.
      // Stops and removes containers
      appcontainer.stop()
      testcontainer.stop()
   }
   
   
   stage '***** Pre-production Stage'
   
   echo '***** Run pre-production Docker Images'

   appimage = docker.image('demoapp:preprod')
   testimage = docker.image('demotest:preprod')

   try
   {
      appcontainer = appimage.run('-d -i -p 8084:8080 --name demoapp_preprod$BUILD_ID')
      testcontainer = testimage.run('-d -i -p 8085:8080 --link demoapp_preprod$BUILD_ID:app --name demotest_preprod$BUILD_ID')

      echo '***** Preparing for testing...Wait for app and test containers to start up'
      retry(10)
      {
         sleep 3
         sh 'docker exec -t demoapp_preprod$BUILD_ID curl --silent --show-error localhost:8080'
         sh 'docker exec -t demotest_preprod$BUILD_ID curl --silent --show-error localhost:8080'
      }

      echo '***** Execute test on test container'
      // This will output results to console output
      sh 'docker exec -t demotest_preprod$BUILD_ID curl --write-out "\n" localhost:8080/test'

      input "Pre-production image ok?"

      echo '***** Image verified...Tagging images for production'
      appimage.tag('prod', true)
      testimage.tag('prod', true)
   }
   catch(all)
   {
      // Force build failure.
      // Don't go any futher after cleanup in finally block
      error 'Pre-production stage failed'
   }
   finally
   {
      // Executes when user aborts from input.
      // Stops and removes containers
      appcontainer.stop()
      testcontainer.stop()
   }

   echo '***** Success...Image ready for production!'
}
