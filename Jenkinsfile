pipeline {
    agent any

    triggers {
        // Periodically check for updates from Git every 5 minutes
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
                echo 'Building project and bypassing all tests...'
                sh "mvn clean package -DskipTests"
            }
        }

        stage('Ansible Deploy') {
            steps {
                echo 'Running Ansible Playbook...'
                sh "ansible-playbook -i 'localhost,' playbook.yml --extra-vars 'app_dir=${env.WORKSPACE}'"
            }
        }
    }

    // Auto-send emails if there is any build/deployment error
    post {
        failure {
            echo 'Pipeline failed! Sending alert emails...'
            emailext (
                subject: "ALERT: Build Error in Jenkins! Job: ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}]",
                body: """<h3>Jenkins Pipeline Failure Report</h3>
                         <p><b>Project:</b> ${env.JOB_NAME}</p>
                         <p><b>Build Number:</b> #${env.BUILD_NUMBER}</p>
                         <p><b>Console Logs URL:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                         <p>Please review the build details above to identify and patch the failure.</p>""",
                to: "srengty@gmail.com",
                recipientProviders: [developers()] // Automatically targets the developer who committed the error
            )
        }
    }
}