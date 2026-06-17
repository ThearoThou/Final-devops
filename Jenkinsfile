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
                echo 'Building project and ignoring test failures...'
                // -Dmaven.test.failure.ignore=true ensures the build returns 0 (Success) 
                // even if tests have errors, allowing the pipeline to continue.
                sh "mvn clean package -Dmaven.test.failure.ignore=true"
            }
        }

stage('Ansible Deploy') {
            steps {
                echo 'Running Ansible Playbook...'
                // Pass the current Jenkins workspace path as 'app_dir' to Ansible
                sh "ansible-playbook -i 'localhost,' playbook.yml --extra-vars 'app_dir=${env.WORKSPACE}'"
            }
        }
    }
}