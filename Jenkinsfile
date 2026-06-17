pipeline {
    agent any

    triggers {
        // Requirements: Periodically check for updates from Git (Poll SCM every 5 minutes)
        pollSCM('*/5 * * * *')
    }

    environment {
        APP_DIR = "/app"
        MAVEN_PATH = "/root/.m2/wrapper/dists/apache-maven-3.9.16/56ba1f9f/bin/mvn"
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Pull code from repository automatically via SCM trigger
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Executing Build and Testing Isolation Stages...'
                // Build and run tests using your working setup configuration
                sh "${MAVEN_PATH} clean test -Dspring.profiles.active=test -Dmaven.test.failure.ignore=false"
            }
        }

        stage('Ansible Deploy') {
            steps {
                echo 'Build and Test succeeded! Running Ansible Playbook to deploy...'
                // Triggers your verified Ansible playbook to deploy to the server
                sh "ansible-playbook -i 'localhost,' ${APP_DIR}/playbook.yml"
            }
        }
    }

    post {
        failure {
            echo 'Pipeline Build Error Detected! Routing notifications...'
            // Requirements: Send email on build error to developer + CC lecturer
            mail to: 'developer-placeholder@domain.com', // Dynamically handled or explicitly targeted
                 cc: 'srengty@gmail.com',
                 subject: "Jenkins CI/CD Build Failure: Pipeline Blocked",
                 body: "Greetings,\n\nThe latest continuous integration pipeline run has failed during build or test execution. Please review your console workspace immediately to fix the build errors."
        }
        success {
            echo 'Pipeline executed completely and successfully!'
        }
    }
}