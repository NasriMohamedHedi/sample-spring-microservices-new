pipeline {
    agent any

    environment {
        SONAR_HOST = "http://localhost:9000"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/sample-spring-microservices-new.git', credentialsId: 'github-pat'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SAST: SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'Jenkins-Token', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn clean verify sonar:sonar \
                            -Dsonar.projectKey=sample-spring-microservices \
                            -Dsonar.projectName='sample-spring-microservices' \
                            -Dsonar.host.url=${SONAR_HOST} \
                            -Dsonar.token=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('SCA: Trivy Filesystem Scan') {
            steps {
                sh 'trivy fs --security-checks vuln --exit-code 1 . || true'
                sh 'trivy fs --security-checks vuln --format json -o trivy-filesystem-report.json . || true'
            }
        }

        stage('Secrets Scan: Gitleaks') {
            steps {
                sh 'gitleaks detect --source . --report-format json --report-path gitleaks-report.json || true'
            }
        }

        stage('Docker Build All Services') {
            steps {
                sh '''
                    docker build -t config-service:latest ./config-service
                    docker build -t discovery-service:latest ./discovery-service
                    docker build -t employee-service:latest ./employee-service
                    docker build -t department-service:latest ./department-service
                    docker build -t organization-service:latest ./organization-service
                    docker build -t gateway-service:latest ./gateway-service
                '''
            }
        }

        stage('Docker Image Scan: Trivy') {
            steps {
                sh '''
                    trivy image --security-checks vuln --exit-code 1 config-service:latest || true
                    trivy image --security-checks vuln --exit-code 1 discovery-service:latest || true
                    trivy image --security-checks vuln --exit-code 1 employee-service:latest || true
                    trivy image --security-checks vuln --exit-code 1 department-service:latest || true
                    trivy image --security-checks vuln --exit-code 1 organization-service:latest || true
                    trivy image --security-checks vuln --exit-code 1 gateway-service:latest || true
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/**, trivy-filesystem-report.json, gitleaks-report.json', onlyIfSuccessful: false
            echo "Pipeline finished. Check SonarQube dashboard and archived reports."
        }
        success {
            echo '✅ Build and all scans completed successfully.'
        }
        failure {
            echo '❌ Build failed — check console and archived reports.'
        }
    }
}

