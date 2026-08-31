pipeline {
    agent {
        label 'agent-java21'
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')
        CHANNEL_ID = "C073YKH0Q8P"
        AWS_REGION = 'ap-south-1'
        DOMAIN = 'recruitcrm'
        DOMAIN_OWNER = '004468257635'

        GIT_COMMITTER_NAME = 'RecruitCRM Engineering'
        GIT_AUTHOR_NAME = 'RecruitCRM Engineering'
        GIT_COMMITTER_EMAIL = 'automations-engineering@recruitcrm.io'
        GIT_AUTHOR_EMAIL = 'automations-engineering@recruitcrm.io'

        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_URL = 'https://recruitcrm-004468257635.d.codeartifact.ap-south-1.amazonaws.com/maven/java-packages/'
        AWS_CODEARTIFACT_REPOSITORY_USERNAME = 'aws'
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID = 'java-packages'
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN = 'recruitcrm'
        ARTIFACT_BUILD_ENVIRONMENT = 'production'
        AWS_ACCOUNT_ID = "004468257635"
        AWS_CODEARTIFACT_REGION = 'ap-south-1'
    }

    stages {
        stage('Update Branches') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials-recruitcrm-engineering', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_TOKEN')]) {
                    sh("""
                        git config --global credential.helper 'store --file=.git/credentials'
                        echo "https://$GIT_USERNAME:$GIT_TOKEN@github.com" > .git/credentials
                        git fetch origin +refs/heads/*:refs/remotes/origin/*
                    """)
                }
                script {
                    def checkoutBranch = ''
                    if (!params.PR_ID.isEmpty() && !params.BASE_BRANCH.isEmpty()) {
                        checkoutBranch = params.BASE_BRANCH
                    } else if (!params.BRANCH_NAME.isEmpty()) {
                        checkoutBranch = params.BRANCH_NAME
                    }
                    def gitCheckout = sh(script: "git checkout ${checkoutBranch}", returnStdout: true).trim()
                    echo "Checkout status: ${gitCheckout}"
                    def currentBranch = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    if (checkoutBranch != currentBranch) {
                        error "Failed to checkout to branch: ${checkoutBranch}"
                    }
                    env.CHECKOUT_BRANCH = checkoutBranch
                }
            }
        }
        stage('Fetch Secrets') {
            steps {
                script {
                    withCredentials([aws(credentialsId: 'candidate-revamp-pipeline-aws-access')]) {
                        sh 'echo "Fetching secrets form AWS Secrets Manager."'
                        def secretsJson = sh(script: """
                            aws secretsmanager get-secret-value --secret-id jenkins/${params.ENV_NAME}/candidate --query SecretString --output text
                        """, returnStdout: true).trim()

                        def secrets = readJSON text: secretsJson

                        secrets.each { key, value ->
                            env."${key}" = value
                        }

                        //Need to get rid of this env value while running tests as it overrides the value in app props
                        env.BACKUP_LOGGING_FILE_PATH = env.LOGGING_FILE_PATH
                        env.LOGGING_FILE_PATH = ""
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
                            --domain $DOMAIN \
                            --domain-owner $DOMAIN_OWNER \
                            --region $AWS_REGION \
                            --query authorizationToken \
                            --output text
                        """,
                            returnStdout: true
                    ).trim()
                }
                sh "echo $CODEARTIFACT_AUTH_TOKEN"
            }
        }
        stage('Environment Preparation') {
            steps {
                script {
                    withEnv(['PATH+VIRTUAL_ENV=/opt/env/bin']) {
                        sh "pip install boto3"
                        sh "python3 ./scripts/create_environment.py"
                        sh "cat src/main/resources/application.properties"
                    }

                    def propertiesList = [
                            "account-id=${params.accountId}",
                            "user-id=${params.userId}"
                    ]
                    def applicationTestProperties = propertiesList.join('\n') + '\n'
                    writeFile file: 'src/test/resources/application-test.properties', text: applicationTestProperties
                }
            }
        }
        stage('Execute Tests') {
            steps {
                sh "mvn -version"
                sh "cat src/main/resources/application.properties"
                sh "cat src/test/resources/application-test.properties"
                sh "mvn -s settings.xml \
                        --batch-mode clean test \
                        surefire-report:report-only \
                        jacoco:report \
                        -Dsentry.maven.plugin.skip=true \
                        -Dmaven.test.failure.ignore=true"
            }
        }
        stage('Publish Test Results') {
            steps {
                script {
                    // Publish the JUnit test results
                    junit '**/target/surefire-reports/TEST-*.xml'

                    // Check the result of the tests and store it in an environment variable
                    def testResult = currentBuild.result ?: 'SUCCESS'
                    echo "Test result is: ${testResult}"
                    env.TEST_RESULT_STATUS = testResult
                }
            }
            post {
                success {
                    script {
                        def buildUrl = env.BUILD_URL.replaceAll("http://jenkins-ci:8080", "https://jenkins.recruitcrm.net")
                        def slackResponse = slackSend(
                                channel: "${CHANNEL_ID}",
                                color: "good",
                                message: "JUnit tests passed! See the test report at ${buildUrl}"
                        )
                    }
                }
                failure {
                    script {
                        def buildUrl = env.BUILD_URL.replaceAll("http://jenkins-ci:8080", "https://jenkins.recruitcrm.net")
                        def slackResponse = slackSend(
                                channel: "${CHANNEL_ID}",
                                color: "danger",
                                message: "JUnit tests failed. See the test report at ${buildUrl}"
                        )
                    }
                }
                unstable {
                    script {
                        def buildUrl = env.BUILD_URL.replaceAll("http://jenkins-ci:8080", "https://jenkins.recruitcrm.net")
                        def slackResponse = slackSend(
                                channel: "${CHANNEL_ID}",
                                color: "warning",
                                message: "Some JUnit tests failed. See the test report at ${buildUrl}"
                        )
                    }
                }
            }
        }
        stage('PR Analysis') {
            when {
                expression {
                    if (!params.PR_ID.isEmpty()) {
                        def validChangeTarget = ["main", "dev", "cse-bug-release"].contains(params.TARGET_BRANCH)
                        def validBaseBranchPrefixes = ['feature-', 'bugfix-', 'bugfix-cse', 'bug-hotfix-', 'enhancement-', 'refactor-']
                        def validChangeBranch = validBaseBranchPrefixes.any { params.BASE_BRANCH.startsWith(it) } || true
                        return validChangeTarget && validChangeBranch
                    }
                    return false
                }
            }
            steps {
                script {
                    def pullRequestId = params.PR_ID
                    def pullRequestBranch = params.BASE_BRANCH
                    def baseBranch = params.TARGET_BRANCH
                    withSonarQubeEnv('default_env') {
                        sh "mvn -s settings.xml \
                                --batch-mode \
                                sonar:sonar \
                                -Dsonar.pullrequest.key=${pullRequestId} \
                                -Dsonar.pullrequest.branch=${pullRequestBranch} \
                                -Dsonar.pullrequest.base=${baseBranch} \
                                -Dmaven.test.skip=true \
                                -Dsentry.maven.plugin.skip=true"
                    }
                }
            }
        }
        stage('Branch Analysis') {
            when {
                expression {
                    def validPrefixes = ['feature-', 'bugfix-', 'bugfix-cse', 'bug-hotfix-', 'enhancement-', 'refactor-']
                    def validBranches = ['main', 'dev', 'cse-bug-release']
                    def validBranch = validPrefixes.any { params.BRANCH_NAME.startsWith(it) } || validBranches.contains(params.BRANCH_NAME) || true
                    return validBranch && params.PR_ID.isEmpty()
                }
            }
            steps {
                script {
                    def currentBranch = params.BRANCH_NAME
                    def targetBranch = 'dev'
                    // https://stackoverflow.com/questions/73883883/jenkinsfile-switch-case-with-contains-is-throwing-cpscallableinvocation-error
                    // We are only handling the cases where the target branch is not dev. Rest will go to dev anyway
                    if (currentBranch.startsWith('bugfix-cse')) {
                        targetBranch = 'cse-bug-release'
                    } else if (currentBranch.startsWith('bug-hotfix')) {
                        targetBranch = 'main'
                    }
                    withSonarQubeEnv('default_env') {
                        sh "mvn -s settings.xml \
                                --batch-mode \
                                sonar:sonar \
                                -Dsonar.branch.name=${currentBranch} \
                                -Dsonar.branch.target=${targetBranch} \
                                -Dmaven.test.skip=true \
                                -Dsentry.maven.plugin.skip=true"
                    }
                }
            }
        }
        stage('Send Test Report') {
            steps {
                script {
                    env.COMMIT_MESSAGE = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    env.COMMIT_DIGEST = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                    echo "Commit message: ${env.COMMIT_MESSAGE}"
                    echo "Commit digest: ${env.COMMIT_DIGEST}"
                }

                sh "zip -r scan.zip target/site/"
                // Change here for new repository
                slackUploadFile(
                        filePath: "scan.zip",
                        credentialId: "slack-bot-token",
                        channel: "${CHANNEL_ID}",
                        initialComment: """
*REPOSITORY_NAME:* recruitcrm-candidate-microservice,
*BRANCH_NAME:* ${env.CHECKOUT_BRANCH},
*COMMIT_URL:* https://github.com/Workforce-Cloud-Tech/recruitcrm-candidate-microservice/commit/${env.COMMIT_DIGEST},
*COMMIT_MESSAGE:* ${env.COMMIT_MESSAGE}
"""
                )
            }
        }
        stage('Semantic Versioning') {
            steps {
                lock("recruitcrm-candidate-microservice-${env.CHECKOUT_BRANCH}") {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'github-credentials-recruitcrm-engineering', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_TOKEN')]) {

                            /*
                            Calculate New Version
                             */
                            sh "npm i"
                            sh "GITHUB_TOKEN=$GIT_TOKEN npx semantic-release --debug --dry-run > semantic-release-dry-run.log 2>&1"
                            sh "cat semantic-release-dry-run.log"
                            env.NEXT_VERSION = sh(
                                    script: "grep -i \"The next release version is\" semantic-release-dry-run.log | awk '{print \$NF}'",
                                    returnStdout: true).trim()
                            echo env.NEXT_VERSION

                            /*
                            Build and Upload New Version to CodeArtifact
                             */
                            if (!env.NEXT_VERSION.isEmpty()) {
                                echo "Setting next version in pom.xml"
                                sh "mvn --batch-mode versions:set -DnewVersion=${env.NEXT_VERSION}-SNAPSHOT"
                            } else {
                                echo "No new version to be released."
                            }
                            buildDevelopment()

                            /*
                            Check and Commit pom.xml
                             */
                            if (!env.NEXT_VERSION.isEmpty()) {
                                def pomChanged = sh(
                                        script: "git status --porcelain pom.xml",
                                        returnStdout: true
                                ).trim()
                                if (pomChanged) {
                                    sh "git add pom.xml"
                                    sh "git commit -m 'chore: Bump version in pom.xml'"
                                    sh "git push origin ${env.CHECKOUT_BRANCH}"
                                }
                            }

                            /*
                            Create Github Release
                             */
                            if (!env.NEXT_VERSION.isEmpty()) {
                                sh "GITHUB_TOKEN=$GIT_TOKEN npx semantic-release --debug"
                            }

                        }
                    }
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
        stage('Pre-Deploy Check') {
            steps {
                script {
                    env.DEPLOY_SERVICE = "TRUE"
                    if (!params.PR_ID.isEmpty()) {
                        echo 'This is a PR Analysis. Skipping deployment.'
                        env.DEPLOY_SERVICE = "FALSE"
                    } else if (params.BRANCH_NAME != params.DEPLOY_BRANCH) {
                        echo "Not the deployment branch. Skipping deployment."
                        env.DEPLOY_SERVICE = "FALSE"
                    } else {
                        echo "Deploying the service..."
                    }
                    echo "BRANCH_NAME = ${params.BRANCH_NAME}"
                    echo "DEPLOY_BRANCH = ${params.DEPLOY_BRANCH}"
                    echo "DEPLOY_SERVICE = ${env.DEPLOY_SERVICE}"
                }
            }
        }
        stage('Creating configuration files') {
            when {
                expression { return env.DEPLOY_SERVICE == "TRUE" }
            }
            steps {
                script {
                    // Restore the logging file path. Do not like this, a very dirty hack
                    env.LOGGING_FILE_PATH = env.BACKUP_LOGGING_FILE_PATH

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
            when {
                expression { return env.DEPLOY_SERVICE == "TRUE" }
            }
            steps {
                script {
                    env.ARTIFACT_NAME = "artifact-${env.BUILD_NUMBER}.zip"
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
                }
            }
        }

        stage('Start Code Deploy') {
            when {
                expression { return env.DEPLOY_SERVICE == "TRUE" }
            }
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

        def CURRENT_ARTIFACT_VERSION = sh(script: 'mvn -s settings.xml help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()

        def VERSION_EXISTS = sh(script: """
            aws codeartifact describe-package-version \
            --region ${env.AWS_REGION} \
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

        if (VERSION_EXISTS != 0) {
            echo "Version does not exist. Proceeding with upload..."
            sh 'mvn -s settings.xml --batch-mode -Dmaven.test.skip=true -Dsentry.maven.plugin.skip=true clean package deploy'
        } else {
            echo "Version already exists. Skipping upload."
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
