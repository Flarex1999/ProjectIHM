pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw && ./mvnw compile -B'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -B -DskipTests'
            }
        }

        stage('Archive') {
            steps {
                // Archive le ZIP complet (JAR + lib/ + launch.bat)
                // Fat JAR : double-clic pour lancer
                archiveArtifacts artifacts: 'target/MessageApp.jar', fingerprint: true
            }
        }
    }

    post {
        success { echo 'ZIP de distribution produit avec succès !' }
        failure { echo 'Build échoué.' }
    }
}
