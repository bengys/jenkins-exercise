job('Build and Push Flask App') {
  
  wrappers {
    credentialsBinding {
        usernamePassword('DOCKERHUB_USERNAME', 'DOCKERHUB_PASSWORD', 'dockerhub-credentials')
    	}
    }


    scm {
        github('bengys/exercise-flask')
    }

    steps {
      
      shell('docker build -t ${DOCKERHUB_USERNAME}/flask-app:latest .')
      shell('echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin')
      shell('docker push ${DOCKERHUB_USERNAME}/flask-app:latest')
                    
    }
}

job('Build and Push Nginx') {
 	

  wrappers {
    credentialsBinding {
        usernamePassword('DOCKERHUB_USERNAME', 'DOCKERHUB_PASSWORD', 'dockerhub-credentials')
    	}
    }


    scm {
        github('bengys/exercise-nginx')
    }

    steps {
      shell('docker build -t ${DOCKERHUB_USERNAME}/nginx-app:latest .')
      shell('echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin')
      shell('docker push ${DOCKERHUB_USERNAME}/nginx-app:latest')
                    
    }
}

job('End to End Test') {
  
  	wrappers {
      credentialsBinding {
          usernamePassword('DOCKERHUB_USERNAME', 'DOCKERHUB_PASSWORD', 'dockerhub-credentials')
          }
    }
 	
    steps {
      shell('docker rm -f flask_app')
      shell('docker rm -f nginx_app')
      shell('docker network rm -f test_network')
      shell('docker network create test_network')
      shell('docker run -d --name=flask_app --network test_network -v /var/run/docker.sock:/var/run/docker.sock -v /usr/bin/docker:/usr/bin/docker ${DOCKERHUB_USERNAME}/flask-app:latest')
      shell('docker run -d --name=nginx_app --network test_network  -p 80:80 ${DOCKERHUB_USERNAME}/nginx-app:latest')
      shell('sleep 10')
      shell('curl --retry 3 --retry-delay 3 localhost')
      shell('docker kill flask_app && docker rm flask_app')
      shell('docker kill nginx_app && docker rm nginx_app')
      shell('docker network rm test_network')
      
                    
    }
}