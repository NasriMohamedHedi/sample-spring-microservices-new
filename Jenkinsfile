pipeline {
    agent any

    environment {
        SONAR_HOST = "http://localhost:9000"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    url: 'https://github.com/NasriMohamedHedi/sample-spring-microservices-new.git', 
                    credentialsId: 'github-pat'
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
                sh 'trivy fs --scanners vuln --format json -o trivy-filesystem-report.json . || true'
            }
        }

        stage('Secrets Scan: Gitleaks') {
            steps {
                sh 'gitleaks detect --source . --report-format json --report-path gitleaks-report.json || true'
            }
        }

        stage('Dockerfile Scan: Trivy Config') {
            steps {
                sh '''
                    find . -type f -iname Dockerfile | while read file; do
                        echo "Scanning $file ..."
                        dir=$(dirname "$file")
                        name=$(basename "$dir")
                        trivy config --exit-code 0 --format table --output trivy-config-$name.txt "$dir"
                    done
                '''
            }
        }
    }

post {
    always {
        archiveArtifacts artifacts: 'target/**, trivy-filesystem-report.json, gitleaks-report.json, trivy-config-*.txt', onlyIfSuccessful: false
        echo "Pipeline finished. Check SonarQube dashboard and archived reports."
    }

    success {
        echo '✅ Build and all scans completed successfully.'

        emailext(
            subject: "✅ Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """
                Hello,

                The Jenkins pipeline *${env.JOB_NAME}* build #${env.BUILD_NUMBER} completed successfully.

                Artifacts and reports are attached.

                Regards,
                Jenkins DevSecOps Pipeline
            """,
            to: "mohamedhedi.nasri@esprit.tn",
            attachmentsPattern: "target/*.jar, trivy-filesystem-report.json, gitleaks-report.json, trivy-config-*.txt"
        )
    }

    failure {
        echo '❌ Build failed — check console and archived reports.'
    }
}
}


