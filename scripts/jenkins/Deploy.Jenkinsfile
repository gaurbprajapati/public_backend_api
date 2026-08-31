pipeline {
    agent {
            kubernetes {
                yaml """
    apiVersion: "v1"
    kind: "Pod"
    metadata:
      annotations:
        karpenter.sh/do-not-disrupt: "true"
      namespace: "jenkins"
    spec:
      serviceAccountName: "java21-sa"
      containers:
      - image: "004468257635.dkr.ecr.ap-south-1.amazonaws.com/jenkins-inbound-agent-java-21"
        imagePullPolicy: "IfNotPresent"
        name: "jnlp"
        resources:
          limits:
            cpu: "3000m"
          requests:
            memory: "3Gi"
            cpu: "2000m"
        tty: true
        volumeMounts:
        - mountPath: "/home/jenkins/agent"
          name: "workspace-volume"
          readOnly: false
        workingDir: "/home/jenkins/agent"
      nodeSelector:
        nodepool: "jenkins-node-pool"
      restartPolicy: "Never"
      securityContext:
        fsGroup: 1000
        runAsGroup: 1000
        runAsUser: 1000
      tolerations:
      - effect: "NoSchedule"
        key: "jenkins"
        operator: "Equal"
        value: "true"
      volumes:
      - emptyDir:
          medium: ""
        name: "workspace-volume"
    """
            }
        }

    environment {
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_URL = 'https://recruitcrm-813287113117.d.codeartifact.ap-south-1.amazonaws.com/maven/java-packages/'
        AWS_CODEARTIFACT_REPOSITORY_USERNAME = 'aws'
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID = 'java-packages'
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN = 'recruitcrm'
        ARTIFACT_BUILD_ENVIRONMENT = 'production'
        AWS_ACCOUNT_ID = "813287113117"
        AWS_CODEARTIFACT_REGION = 'ap-south-1'
        AWS_CREDENTIALS = credentials('candidate-revamp-pipeline-aws-access')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    println("Reconfiguring the pipelines based on parameters")
                    if (env.JOB_BASE_NAME != "${params.ENV_NAME}_contract-staffing-timesheet") {
                        error("Incorrect environment name. Please use the correct environment name")
                    }
                    jobDsl targets: 'scripts/jenkins/dsl_deploy.groovy'

                    println("Pipeline reconfigured successfully")
                }
            }
        }

        stage('Fetch Secrets') {
            steps {
                script {
                    withCredentials([aws(credentialsId: 'candidate-revamp-pipeline-aws-access')]) {
                        sh 'echo "Fetching secrets form AWS Secrets Manager."'
                        def secretsJson = sh(script: """
                            aws secretsmanager get-secret-value --secret-id jenkins/${params.ENV_NAME}/contract-staffing-timesheet --query SecretString --output text --region ap-south-1
                        """, returnStdout: true).trim()

                        def secrets = readJSON text: secretsJson

                        secrets.each { key, value ->
                            env."${key}" = value
                        }
                    }
                }
            }
        }

        stage('Download artifact') {
            steps {
                script {
                    def packageVersion = params.PACKAGE_VERSION.replaceFirst(/^v/, "")
                    echo "Fetching artifact for version: ${packageVersion}"

                    sh """
                    aws codeartifact get-package-version-asset \
                        --region ${env.AWS_CODEARTIFACT_REGION} \
                        --domain ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN} \
                        --domain-owner ${env.AWS_ACCOUNT_ID} \
                        --repository ${env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID} \
                        --format maven \
                        --namespace io.recruitcrm.microservice \
                        --package timesheet-microservice \
                        --package-version ${packageVersion} \
                        --asset timesheet-microservice-${packageVersion}.jar \
                        timesheet-microservice.jar 
                    """
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

                    sh 'mkdir -p templates/processed/timesheet-service'
                    sh 'envsubst < templates/raw/timesheet-service/timesheet-service.tmpl > templates/processed/timesheet-service/timesheet-service.service'
                    sh 'envsubst < templates/raw/timesheet-service/timesheet-service-nr.tmpl > templates/processed/timesheet-service/timesheet-service-nr.service'

                    sh 'mkdir -p templates/processed/new-relic'
                    sh 'cp  templates/raw/new-relic/newrelic.tmpl templates/processed/new-relic/newrelic.tmpl'
                    sh 'envsubst < templates/raw/new-relic/newrelic-vars.tmpl > templates/processed/new-relic/newrelic-vars.env'
                    sh 'envsubst < templates/raw/otel_config.tmpl > templates/processed/config.yaml'
                    sh 'echo "Config files created successfully..."'
                }
            }
        }


        stage('Upload to S3 bucket') {
            steps {
                script {
                    def shortId = UUID.randomUUID().toString().replaceAll("-", "").take(10)
                    env.ARTIFACT_NAME = "artifact-${env.BUILD_NUMBER}-${shortId}.zip"
                    sh "zip -r ${env.ARTIFACT_NAME} ."

                    if (env.DEPLOY_MUMBAI == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_MUMBAI}/${params.ENV_NAME}/contract-staffing-timesheet/ --sse aws:kms"
                    }
                    if (env.DEPLOY_IRELAND == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_IRELAND}/${params.ENV_NAME}/contract-staffing-timesheet/ --sse aws:kms"
                    }
                    if (env.DEPLOY_VIRGINIA == 'true') {
                        sh "aws s3 cp ${env.ARTIFACT_NAME} s3://${env.S3_CODE_DEPLOY_ARTIFACT_BUCKET_VIRGINIA}/${params.ENV_NAME}/contract-staffing-timesheet/ --sse aws:kms"
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
            }
        }
    }
}

def deployService(CODE_DEPLOY_ARTIFACT_BUCKET, DEPLOYMENT_REGION) {
    echo "Starting code deploy"

    def DEPLOYMENT_ID = sh(script: """
        aws deploy create-deployment \
        --application-name ${env.CODE_DEPLOY_APPLICATION_NAME} \
        --deployment-group-name tf-${params.ENV_NAME}-contract-staffing-timesheet \
        --s3-location bucket=${CODE_DEPLOY_ARTIFACT_BUCKET},bundleType=zip,key=${params.ENV_NAME}/contract-staffing-timesheet/${env.ARTIFACT_NAME} \
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