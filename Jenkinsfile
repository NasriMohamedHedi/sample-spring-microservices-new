pipeline {
    agent any

    environment {
        SONAR_HOST = "http://localhost:9000"
        SONAR_TOKEN = credentials('Jenkins-Token')
        TRIVY_FS_REPORT = "trivy-fs-report.txt"
        TRIVY_IMAGE_REPORT = "trivy-image-report.txt"
        GITLEAKS_REPORT = "gitleaks-report.json"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr:'50'))
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/NasriMohamedHedi/sample-spring-microservices-new.git'
            }
        }

        stage('Build All Modules') {
            steps {
                // Build all modules, skip tests to save time
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn -B test || true'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SAST: SonarQube Analysis') {
            steps {
                // Maven will automatically detect binaries for all modules
                sh """
                    mvn clean verify sonar:sonar \
                      -Dsonar.projectKey=sample-spring-microservices \
                      -Dsonar.projectName='sample-spring-microservices' \
                      -Dsonar.host.url=${SONAR_HOST} \
                      -Dsonar.token=${SONAR_TOKEN}
                """
            }
        }

        stage('SCA: Trivy Filesystem Scan') {
            steps {
                sh """
                    docker run --rm -v "${WORKSPACE}":/project aquasecurity/trivy:latest fs \
                    --exit-code 1 --severity HIGH,CRITICAL /project > ${TRIVY_FS_REPORT} || true
                """
            }
            post {
                always {
                    archiveArtifacts artifacts: "${TRIVY_FS_REPORT}", onlyIfSuccessful: false
                }
            }
        }

        stage('Secrets Scan: Gitleaks') {
            steps {
                sh """
                    docker run --rm -v "${WORKSPACE}":/src zricethezav/gitleaks:latest detect \
                    --source /src --report-format json --report-path /src/${GITLEAKS_REPORT} --no-git || true
                """
            }
            post {
                always {
                    archiveArtifacts artifacts: "${GITLEAKS_REPORT}", onlyIfSuccessful: false
                }
            }
        }

        stage('Docker Build All Services') {
            steps {
                // Loop through services and build docker images
                script {
                    def services = ['discovery-service','config-service','employee-service','department-service','organization-service','gateway-service']
                    for (s in services) {
                        sh "docker build -t ${s}:ci-${env.BUILD_NUMBER} ./${s}"
                    }
                }
            }
        }

        stage('Docker Image Scan: Trivy') {
            steps {
                script {
                    def services = ['discovery-service','config-service','employee-service','department-service','organization-service','gateway-service']
                    for (s in services) {
                        sh """
                            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                            -v "${WORKSPACE}":/workdir aquasecurity/trivy:latest image \
                            --exit-code 1 --severity HIGH,CRITICAL ${s}:ci-${env.BUILD_NUMBER} > ${TRIVY_IMAGE_REPORT} || true
                        """
                        archiveArtifacts artifacts: "${TRIVY_IMAGE_REPORT}", onlyIfSuccessful: false
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/**, **/*.txt, **/*.json', onlyIfSuccessful: false
            echo "Pipeline finished. Check SonarQube dashboard and archived reports."
        }
        failure {
            echo '❌ Build failed — check console and archived reports.'
        }
    }
}

