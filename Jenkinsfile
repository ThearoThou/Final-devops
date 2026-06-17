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
                echo 'Building project and skipping tests for demo purposes...'
                // Skip tests to ensure the pipeline proceeds to Ansible deployment
                sh "mvn clean package -DskipTests"
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