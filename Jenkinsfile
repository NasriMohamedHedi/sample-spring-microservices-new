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
                sh 'trivy fs --security-checks vuln --exit-code 0 --format table . || true'
                sh 'trivy fs --security-checks vuln --format json -o trivy-filesystem-report.json . || true'
            }
        }

        stage('Secrets Scan: Gitleaks') {
            steps {
                sh 'gitleaks detect --source . --report-format json --report-path gitleaks-report.json || true'
            }
        }

        stage('Dockerfile Scan (No Image Build)') {
            steps {
                echo "🔍 Scanning Dockerfiles for misconfigurations..."
                sh '''
                    # Scan all Dockerfiles across services
                    find . -type f -iname "Dockerfile" | while read file; do
                        echo "Scanning $file ..."
                        trivy config --exit-code 0 --format table --output "trivy-config-$(basename $(dirname $file)).txt" "$(dirname $file)" || true
                    done
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/**, *.json, *.txt', onlyIfSuccessful: false
            echo "📦 Pipeline finished. Check SonarQube dashboard and archived reports."
        }
        success {
            echo '✅ Build and all scans completed successfully.'
        }
        failure {
            echo '❌ Build failed — check console and archived reports.'
        }
    }
}

