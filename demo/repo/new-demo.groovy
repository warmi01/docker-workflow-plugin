
node {
   git '/tmp/repo'

   docker.withRegistry('https://docker.example.com/', 'docker-registry-login') {

     stage 'contract'
     sh 'cp demoapp/Docker_v' + APP_VERSION + ' demoapp/Dockerfile'
     def app_image = docker.build('demoapp','demoapp')
     def app_container_8082 = app_image.run('-i -p 8082:8080 --name demoapp_8082')

     sh 'cp demotest/Docker_Contract demotest/Dockerfile'
     def contract_image = docker.build('demotest_con:2.3','demotest')
     def contract_container = contract_image.run('-i -p 8084:8080 --link demoapp_8082:app --name demotest_rt')
     input "How does contract look?"
     contract_container.stop()

     stage 'integration'

     try {
        docker.script.sh "docker inspect -f . demoapp_8083"
        docker.script.sh "docker stop demoapp_8083 && docker rm -f demoapp_8083"
     } catch (hudson.AbortException e) {
     }

     def app_container_8083 = app_image.run('-i -p 8083:8080 --name demoapp_8083')
     def integration_image = docker.build('demotest_integration:1.4','demotest')
     def integration_container = integration_image.run('-i -p 8084:8080 --link demoapp_8083:app --name demotest_rt')

     input "How does integration look?"
     app_container_8082.stop()
     integration_container.stop()

   }
}

