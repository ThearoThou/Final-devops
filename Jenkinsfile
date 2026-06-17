pipeline {
    agent any

    triggers {
        // Periodically check for updates from Git every 5 minutes
        pollSCM('*/5 * * * *')
    }

    environment {
        APP_DIR = "/app"
        MAVEN_PATH = "/root/.m2/wrapper/dists/apache-maven-3.9.16/56ba1f9f/bin/mvn"
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Fixing permissions and executing Build and Testing Isolation Stages...'
                // Grant execute permission to the Maven wrapper to resolve 'Permission denied' (exit code 126)
                sh "chmod +x ${env.MAVEN_PATH}"
                // Build and run tests
                sh "${env.MAVEN_PATH} clean test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=false"
            }
        }

        stage('Ansible Deploy') {
            steps {
                echo 'Build and Test succeeded! Running Ansible Playbook to deploy...'
                // Run the verified Ansible playbook [cite: 3, 62]
                sh "ansible-playbook -i 'localhost,' ${env.APP_DIR}/playbook.yml"
            }
        }
    }

    post {
        failure {
            echo 'Pipeline Build Error Detected!'
            // Mail notification is commented out due to lack of local SMTP relay configuration
            /*
            mail to: 'developer@example.com',
                 cc: 'srengty@gmail.com',
                 subject: "Jenkins CI/CD Build Failure: Pipeline Blocked",
                 body: "The pipeline has failed during build or test execution."
            */
        }
        success {
            echo 'Pipeline executed completely and successfully!'
        }
    }
}