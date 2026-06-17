pipeline {
    agent any

    triggers {
        pollSCM('*/5 * * * *')
    }

    environment {
        APP_DIR = "/app"
        // Simply use 'mvn' as it is likely in the system PATH
        MAVEN_BIN = "mvn"
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Executing Build and Testing Isolation Stages...'
                // Run build using the system maven command
                sh "${env.MAVEN_BIN} clean test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=false"
            }
        }

        stage('Ansible Deploy') {
            steps {
                echo 'Build and Test succeeded! Running Ansible Playbook to deploy...'
                sh "ansible-playbook -i 'localhost,' ${env.APP_DIR}/playbook.yml"
            }
        }
    }

    post {
        failure {
            echo 'Pipeline Build Error Detected!'
        }
        success {
            echo 'Pipeline executed completely and successfully!'
        }
    }
}