pipeline {
    agent any

    triggers {
        pollSCM('*/5 * * * *')
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Executing Build and Testing...'
                // Now that we installed it, 'mvn' will be found
                sh "mvn clean test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=false"
            }
        }

        stage('Ansible Deploy') {
            steps {
                echo 'Running Ansible Playbook...'
                // 'ansible-playbook' will now be found
                sh "ansible-playbook -i 'localhost,' playbook.yml"
            }
        }
    }
}