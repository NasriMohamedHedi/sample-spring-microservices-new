pipeline {
    agent any

    environment {
        SONAR_HOST = "http://192.168.186.157:9000"
        IMAGE_NAME_PREFIX = "microservice"
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

        stage('Build All Services') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn -B test || true'
            }
            post {
                always { junit '**/target/surefire-reports/*.xml' }
            }
        }

        stage('SAST: SonarQube Analysis') {
            environment {
                SONAR_TOKEN = credentials('sonar-token') // your Jenkins secret text ID
            }
            steps {
                sh """
                    mvn -B sonar:sonar \
                        -Dsonar.projectKey=sample-spring-microservices \
                        -Dsonar.host.url=${SONAR_HOST} \
                        -Dsonar.login=${SONAR_TOKEN} \
                        -Dsonar.java.binaries=**/target/classes
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
                script {
                    // iterate over each service folder
                    def services = ['discovery-service','config-service','employee-service','department-service','organization-service','gateway-service']
                    services.each { svc ->
                        def imageName = "${IMAGE_NAME_PREFIX}-${svc}:${env.BUILD_NUMBER}"
                        sh "docker build -t ${imageName} ${svc}"
                    }
                }
            }
        }

        stage('Docker Image Scan: Trivy') {
            steps {
                script {
                    def services = ['discovery-service','config-service','employee-service','department-service','organization-service','gateway-service']
                    services.each { svc ->
                        def imageName = "${IMAGE_NAME_PREFIX}-${svc}:${env.BUILD_NUMBER}"
                        sh """
                            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                                -v "${WORKSPACE}":/workdir aquasecurity/trivy:latest image \
                                --exit-code 1 --severity HIGH,CRITICAL ${imageName} > ${TRIVY_IMAGE_REPORT}-${svc} || true
                        """
                        archiveArtifacts artifacts: "${TRIVY_IMAGE_REPORT}-${svc}", onlyIfSuccessful: false
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/**, **/*.txt, **/*.json', onlyIfSuccessful: false
            echo "Pipeline finished. Check SonarQube dashboard and archived reports."
        }
        failure {
            mail to: 'nasrimohamedhedi0@gmail.com', subject: "Jenkins build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}", body: "Check Jenkins console and archived artifacts."
        }
    }
}

