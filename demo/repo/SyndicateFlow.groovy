// Test to see Jenkins workflow build parameter has been defined
try
{
   APP_VERSION
   echo '***** Workflow running for demoapp v' + APP_VERSION
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
         sh 'docker exec -t demoapp_contract$BUILD_ID curl --write-out "\n" localhost:8080/status'
         sh 'docker exec -t demotest_contract$BUILD_ID curl --write-out "\n" localhost:8080/status'
      }

      echo '***** Execute test on test container'
      // This will execute the test from inside the test container.
      // The output will be piped to a file so that it can be read into a
      // variable to process the test output.
      sh 'docker exec -t demotest_contract$BUILD_ID curl --write-out "\n" localhost:8080/test > test.txt'
      def testoutput = readFile('test.txt')
      echo testoutput

      if (testoutput.contains('pass: true'))
      {
         input 'Contract test passed. Continue?'
      }
      else
      {
         error 'Contract test failed...Aborting build.'
      }

      echo '***** Image verified...Tagging app image for integration'
      appimage.tag('integration', true)
   }
   catch (all)
   {
      // Force build failure.
      // Don't go any futher after cleanup in finally block
      error 'Contract stage failed'
   }
   finally
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
      
      try
      {
         testcontainer.stop()          
      }
      catch (all)
      {
      }
   }
   
   
   stage '***** Integration Stage'
   
   echo '***** Building Docker integration test image'
   sh 'cp demotest/Docker_Integration demotest/Dockerfile'
   testimage = docker.build('demotest:integration','demotest')
   
   echo '***** Run integration Docker Images'

   appimage = docker.image('demoapp:integration')

   try
   {
      sh 'docker stop demoapp_integration && docker rename demoapp_integration demoapp_integration_prev'
      echo '***** Backed up previous integration container'       
   }
   catch (all)
   {
   }
   
   try
   {
      appcontainer = appimage.run('-d -i -p 8084:8080 --name demoapp_integration')
      testcontainer = testimage.run('-d -i -p 8085:8080 --link demoapp_integration:app --name demotest_integration$BUILD_ID')

      echo '***** Preparing for integration testing...Wait for app and test containers to start up'
      retry(10)
      {
         sleep 3
         sh 'docker exec -t demoapp_integration curl --write-out "\n" localhost:8080/status'
         sh 'docker exec -t demotest_integration$BUILD_ID curl --write-out "\n" localhost:8080/status'
      }

      echo '***** Execute test on test container'
      // This will execute the test from inside the test container.
      // The output will be piped to a file so that it can be read into a
      // variable to process the test output.
      sh 'docker exec -t demotest_integration$BUILD_ID curl --write-out "\n" localhost:8080/test > test.txt'
      def testoutput = readFile('test.txt')
      echo testoutput

      if (testoutput.contains('pass: true'))
      {
         input 'Integration test passed. Continue?'
      }
      else
      {
         input 'Integration test failed. (Pausing)'
         error 'Integration test failed...Aborting build.'
      }

      echo '***** Image verified...Tagging app image for production'
      appimage.tag('prod', true)
      
      try
      {
         // Remove backup of old integration container
         sh 'docker rm demoapp_integration_prev'          
      }
      catch(all)
      {
      }
   }
   catch(all)
   {
      echo 'Integration stage failed...Rolling back to previous version'
      
      // Remove current demoapp container
      try
      {
         appcontainer.stop()          
      }
      catch (all2)
      {
      }
      
      // Start the previous container
      try
      {
         sh 'docker rename demoapp_integration_prev demoapp_integration && docker start demoapp_integration'          
      }
      catch (all2)
      {
      }
      
      // Force build failure.
      // Don't go any futher after cleanup in finally block
      error 'Integration stage failed'
   }
   finally
   {
      // Executes when user aborts from input.
      // Stops and removes containers
      try
      {
         testcontainer.stop()
      }
      catch (all)
      {
      }
   }

   // The current integration container will continue to run at this point 
   
   echo '***** Success...Image ready for production!'
}
