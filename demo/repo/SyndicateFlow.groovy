node
{
   git '/tmp/repo'

   stage '***** Integration Stage'
   
   sh 'cp demoapp/Docker_v1.0 demoapp/Dockerfile'
   sh 'cp demotest/Docker_Integration demotest/Dockerfile'

   def appimage
   def testimage
   def appcontainer
   def testcontainer

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

      input "Integration image ok?"

      echo '***** Image verified...Tagging images for preproduction'
      appimage.tag('preprod', true)
      testimage.tag('preprod', true)
   }
   catch(all)
   {
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

      input "Pre-production image ok?"

      echo '***** Image verified...Tagging images for production'
      appimage.tag('prod', true)
      testimage.tag('prod', true)
   }
   catch(all)
   {
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
