podTemplate(label: 'java-8', containers: [
    containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat', args: ''),
    containerTemplate(name: 'docker', image: 'docker:17.09', ttyEnabled: true, command: 'cat', args: '' )
    ],
    volumes: [hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')]
)

{
    node('java-8') {
        
        stage('Build') {
            git branch: 'master', url: 'https://github.intuit.com/payments/http-hystrix.git', credentialsId: "github-svc-sbseg-ci"
            container('maven') {
                stage('Build jar') {
                    sh './gradlew clean build'
                }
            }
        }  
    }
}