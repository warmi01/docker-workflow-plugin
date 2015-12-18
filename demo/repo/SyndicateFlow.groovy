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

   stage '***** Contract Stage'
   
   // Get app version from Jenkins build parameter
   sh 'cp demoapp/Docker_v' + APP_VERSION + ' demoapp/Dockerfile'
   sh 'cp demotest/Docker_Contract demotest/Dockerfile'

   def appimage
   def testimage
   def appcontainer
   def testcontainer

   echo '***** Build app and test Docker images'
   parallel "Building Docker app image":
   {
      appimage = docker.build('demoapp:contract','demoapp')
   },
   "Building Docker contract test image":
   {
      testimage = docker.build('demotest:contract','demotest')
   },
   failFast: true
   
   echo '***** Run contract Docker Images'
   try
   {
      appcontainer = appimage.run('-d -i -p 8082:8080 --name demoapp_contract$BUILD_ID')
      testcontainer = testimage.run('-d -i -p 8083:8080 --link demoapp_contract$BUILD_ID:app --name demotest_contract$BUILD_ID')

      echo '***** Preparing for contract testing...Wait for app and test containers to start up'
      retry(10)
      {
         sleep 3
         sh 'docker exec -t demoapp_contract$BUILD_ID curl --silent --show-error localhost:8080'
         sh 'docker exec -t demotest_contract$BUILD_ID curl --silent --show-error localhost:8080'
      }

      echo '***** Execute test on test container'
      // This will output results to console output
      sh 'docker exec -t demotest_contract$BUILD_ID curl --write-out "\n" localhost:8080/test'

      input "Contract image ok?"

      echo '***** Image verified...Tagging app image for integration'
      appimage.tag('integration', true)
   }
   catch(all)
   {
      // Force build failure.
      // Don't go any futher after cleanup in finally block
      error 'Contract stage failed'
   }
   finally
   {
      // Executes when user aborts from input.
      // Stops and removes containers
      appcontainer.stop()
      testcontainer.stop()
   }
   
   
   stage '***** Integration Stage'
   
   echo '***** Building Docker integration test image'
   sh 'cp demotest/Docker_Integration demotest/Dockerfile'
   testimage = docker.build('demotest:integration','demotest')
   
   echo '***** Run integration Docker Images'

   appimage = docker.image('demoapp:integration')

   try
   {
      appcontainer = appimage.run('-d -i -p 8084:8080 --name demoapp_integration$BUILD_ID')
      testcontainer = testimage.run('-d -i -p 8085:8080 --link demoapp_integration$BUILD_ID:app --name demotest_integration$BUILD_ID')

      echo '***** Preparing for integration testing...Wait for app and test containers to start up'
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

      echo '***** Image verified...Tagging images for production'
      appimage.tag('prod', true)
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

   echo '***** Success...Image ready for production!'
}
