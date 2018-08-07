podTemplate(label: 'java-8', containers: [
    containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat', args: ''),
    containerTemplate(name: 'docker', image: 'docker:17.09', ttyEnabled: true, command: 'cat', args: '' )
    ],
    volumes: [hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')]
)

{
    node('java-8') {
        
        stage('Build') {
            def scmInto = checkout([$class: 'GitSCM', branches: [[name: 'master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github-svc-sbseg-ci', url: 'https://github.intuit.com/payments/http-hystrix.git']]])
            container('maven') {
                stage('Build jar') {
                    withCredentials([file(credentialsId: 'qbo-settings.xml', variable: 'QBO_NEXUS_SETTINGS')]) {
                        sh "./gradlew -Pmaven.settings.location=$QBO_NEXUS_SETTINGS/settings.xml clean build publish"
                    }
                }
            }
        }
    }
}