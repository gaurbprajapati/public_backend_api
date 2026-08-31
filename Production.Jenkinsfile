pipeline {
    agent {
        node {
            label 'agent-java21'
            customWorkspace "${JENKINS_HOME}/workspace/${JOB_NAME}/${BUILD_NUMBER}"
        }
    }

    post {
        cleanup {
            deleteDir()
            dir("${workspace}@tmp") {
                deleteDir()
            }
            dir("${workspace}@script") {
                deleteDir()
            }
        }
    }

    environment {
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_URL = 'https://recruitcrm-004468257635.d.codeartifact.ap-south-1.amazonaws.com/maven/java-packages/'
        AWS_CODEARTIFACT_REPOSITORY_USERNAME = 'aws'
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID = 'java-packages'
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN = 'recruitcrm'
        ARTIFACT_BUILD_ENVIRONMENT = 'production'
        AWS_ACCOUNT_ID = "004468257635"
        AWS_CODEARTIFACT_REGION = 'ap-south-1'
        AWS_REGION = 'ap-south-1'
    }

    stages {
        stage('Fetch Secrets') {
            steps {
                script {
                    def region = params.ENV_NAME == 'canada' ? 'ca-central-1' : 'ap-south-1'
                    echo "Secrets Region: ${region}"

                    withCredentials([aws(credentialsId: 'candidate-revamp-pipeline-aws-access')]) {
                        sh 'echo "Fetching secrets form AWS Secrets Manager."'
                        def secretsJson = sh(script: """
                            aws secretsmanager get-secret-value --secret-id jenkins/${params.ENV_NAME}/candidate --query SecretString --output text --region ${region}
                        """, returnStdout: true).trim()

                        def secrets = readJSON text: secretsJson

                        secrets.each { key, value ->
                            env."${key}" = value
                        }
                    }
                }
                sh 'printenv | sort'
            }
        }

        stage('Inject AWS Credentials') {
            steps {
                script {
                    // Use withCredentials to load AWS credentials into environment variables
                    withCredentials([aws(credentialsId: 'candidate-revamp-pipeline-aws-access')]) {
                        // Set credentials as global environment variables
                        env.AWS_ACCESS_KEY_ID = "${AWS_ACCESS_KEY_ID}"
                        env.AWS_SECRET_ACCESS_KEY = "${AWS_SECRET_ACCESS_KEY}"
                    }
                }
                script {
                    // Fetch the token and inject it into the environment
                    env.CODEARTIFACT_AUTH_TOKEN = sh(
                            script: """
                        aws codeartifact get-authorization-token \
                            --domain ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN} \
                            --domain-owner ${env.AWS_ACCOUNT_ID} \
                            --region ${env.AWS_CODEARTIFACT_REGION} \
                            --query authorizationToken \
                            --output text
                        """,
                            returnStdout: true
                    ).trim()
                }
                sh "echo $CODEARTIFACT_AUTH_TOKEN"
            }
        }

        stage('Build Artifact') {
            steps {
                script {
                    buildProduction()
                }
            }
            post {
                failure {
                    echo "Build failed."
                }
                success {
                    echo "Build succeeded."
                }
            }
        }

        stage("Manual Approval Stage") {
            steps {
                script {
                    timeout(time: 15, unit: "MINUTES") {
                        input message: 'Do you want to approve the deployment - Candidate-Microservice ?', ok: 'Yes'
                    }
                }
            }
        }

        stage('Creating configuration files') {
            steps {
                script {
                    sh 'echo "Generating configuration files..."'
                    sh 'mkdir -p templates/processed'
                    sh 'envsubst < templates/raw/application.properties.tmpl > templates/processed/application.properties'
                    sh 'envsubst < templates/raw/nr_enabled.tmpl > templates/processed/nr_enabled.env'
                    sh 'envsubst < templates/raw/filebeat.tmpl > templates/processed/filebeat.yml'

                    sh 'mkdir -p templates/processed/candidate-service'
                    sh 'envsubst < templates/raw/candidate-service/candidate-service.tmpl > templates/processed/candidate-service/candidate-service.service'
                    sh 'envsubst < templates/raw/candidate-service/candidate-service-nr.tmpl > templates/processed/candidate-service/candidate-service-nr.service'

                    sh 'mkdir -p templates/processed/new-relic'
                    sh 'envsubst < templates/raw/new-relic/newrelic.tmpl > templates/processed/new-relic/newrelic.yml'
                    sh 'echo "Config files created successfully..."'
                }
            }
        }


        stage('Upload to S3 bucket') {
            steps {
                script {
                    def shortId = UUID.randomUUID().toString().replaceAll("-", "").take(10)
                    env.ARTIFACT_NAME = "artifact-${env.BUILD_NUMBER}-${shortId}.zip"
                    // Fetch the token and inject it into the environment
                    sh 'cp "$(ls target/*.jar | head -n 1)" candidate-microservice.jar'
                    sh 'rm -rf target'
                    sh "zip -r ${env.ARTIFACT_NAME} ."

                    if (env.DEPLOY_MUMBAI == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_MUMBAI}/${params.ENV_NAME}/candidate/ --sse aws:kms"
                    }
                    if (env.DEPLOY_VIRGINIA == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_VIRGINIA}/${params.ENV_NAME}/candidate/ --sse aws:kms"
                    }
                    if (env.DEPLOY_IRELAND == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_IRELAND}/${params.ENV_NAME}/candidate/ --sse aws:kms"
                    }
                    if (env.DEPLOY_CANADA == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_CANADA}/${params.ENV_NAME}/candidate/ --sse aws:kms"
                    }
                }
            }
        }

        stage("Deployment") {
            parallel {
                stage('Mumbai') {
                    when {
                        expression { env.DEPLOY_MUMBAI == 'true' }
                    }
                    steps {
                        script {
                            deployService(env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_MUMBAI, 'ap-south-1')
                        }
                    }
                }
                stage('Virginia') {
                    when {
                        expression { env.DEPLOY_VIRGINIA == 'true' }
                    }
                    steps {
                        script {
                            deployService(env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_VIRGINIA, 'us-east-1')
                        }
                    }
                }
                stage('Ireland') {
                    when {
                        expression { env.DEPLOY_IRELAND == 'true' }
                    }
                    steps {
                        script {
                            deployService(env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_IRELAND, 'eu-west-1')
                        }
                    }
                }
                stage('Canada') {
                    when {
                        expression { env.DEPLOY_CANADA == 'true' }
                    }
                    steps {
                        script {
                            deployService(env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_CANADA, 'ca-central-1')
                        }
                    }
                }
            }
        }
    }
}

def buildProduction() {
    try {
        echo "Starting production build..."
        sh 'mvn -s settings.xml --batch-mode versions:set -DremoveSnapshot=true'
        sh 'mvn -s settings.xml --batch-mode versions:use-releases -DfailIfNotReplaced=true'

        // The sed command is used to remove ANSI escape codes from the output
        def CURRENT_ARTIFACT_VERSION = sh(script: 'mvn -s settings.xml help:evaluate -Dexpression=project.version -q -DforceStdout | sed "s/\\x1b\\[[0-9;]*[a-zA-Z]//g"', returnStdout: true).trim()

        def VERSION_EXISTS = sh(script: """
            aws codeartifact describe-package-version \
            --region ${env.AWS_CODEARTIFACT_REGION} \
            --domain ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN} \
            --domain-owner ${env.AWS_ACCOUNT_ID} \
            --repository ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID} \
            --format maven \
            --package "candidate-microservice" \
            --namespace "io.recruitcrm.microservice" \
            --package-version ${CURRENT_ARTIFACT_VERSION} \
            --query 'packageVersion.version' \
            --output text 2>/dev/null
        """, returnStatus: true)

        echo "Version exists: ${VERSION_EXISTS}"
        echo "Current artifact version: ${CURRENT_ARTIFACT_VERSION}"
        echo "CodeArtifact repository domain: ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN}"
        echo "CodeArtifact repository id: ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID}"
        echo "region: ${env.AWS_CODEARTIFACT_REGION}"

        if (VERSION_EXISTS != 0) {
            echo "Version does not exist. Proceeding with upload..."
            sh 'mvn -s settings.xml --batch-mode -Dmaven.test.skip=true -Dsentry.maven.plugin.skip=true clean package deploy'
        } else {
            echo "Version already exists. Skipping upload."
            sh 'mvn -s settings.xml --batch-mode -Dmaven.test.skip=true -Dsentry.maven.plugin.skip=true clean package'
        }

    } catch (Exception e) {
        error "Build failed with profile: production"
    }
}

def buildDevelopment() {
    try {
        echo "Starting development build..."
        sh 'mvn -s settings.xml --batch-mode -Dmaven.test.skip=true -Dsentry.maven.plugin.skip=true clean package deploy'
    } catch (Exception e) {
        error "Build failed with profile: development"
    }
}

def deployService(CODE_DEPLOY_ARTIFACT_BUCKET, DEPLOYMENT_REGION) {
    echo "Starting code deploy"

    def DEPLOYMENT_ID = sh(script: """
        aws deploy create-deployment \
        --application-name ${env.CODE_DEPLOY_APPLICATION_NAME} \
        --deployment-group-name tf-${params.ENV_NAME}-candidate \
        --s3-location bucket=${CODE_DEPLOY_ARTIFACT_BUCKET},bundleType=zip,key=${params.ENV_NAME}/candidate/${env.ARTIFACT_NAME} \
        --region ${DEPLOYMENT_REGION} \
        --output text --query 'deploymentId'
    """, returnStdout: true).trim()

    echo "Deployment started with ID: ${DEPLOYMENT_ID}"
    echo "URL: https://${DEPLOYMENT_REGION}.console.aws.amazon.com/codesuite/codedeploy/deployments/${DEPLOYMENT_ID}"

    echo "Waiting for deployment to complete..."

    def deployStatus = ""
    timeout(time: 20, unit: 'MINUTES') {
        while (true) {
            deployStatus = sh(script: """
            aws deploy get-deployment \
            --deployment-id ${DEPLOYMENT_ID} \
            --region ${DEPLOYMENT_REGION} \
            --output text --query 'deploymentInfo.status'
            """, returnStdout: true).trim()

            echo "Current Deployment Status: ${deployStatus}"

            if (deployStatus == "Succeeded") {
                currentBuild.result = 'SUCCESS'
                break
            } else if (deployStatus == "Failed" || deployStatus == "Stopped") {
                currentBuild.result = 'FAILURE'
                error("Deployment failed with status: ${deployStatus}")
            }

            // Sleep for 30 seconds before checking the status again
            sleep 30
        }
    }
}