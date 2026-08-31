@Library('jenkins-shared-library@main') _

pipeline {
    agent {
        kubernetes {
            yaml libraryResource('kubernetes/java-21-build-agent.yml')
        }
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')

        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_URL = credentials('codeartifact-repo-url')
        AWS_CODEARTIFACT_REPOSITORY_USERNAME = credentials('codeartifact-repo-username')
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_ID = credentials('codeartifact-repo-id')
        JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN = credentials('codeartifact-repo-domain')
        AWS_ACCOUNT_ID = credentials('aws-account-id-prod')
        AWS_CODEARTIFACT_REGION = credentials('aws-codeartifact-region')

        REPOSITORY_NAME = 'contract-staffing-timesheet-microservice'

        accountId = '459515'
        userId = '459515'
    }

    options {
        // disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    prepareBuildContext(
                        serviceName: 'contract-staffing-timesheet',
                        gitCredentialsId: 'github-credentials-recruitcrm-engineering'
                    )
                }
            }
        }
        stage('Parallel-1') {
            parallel {
                stage('Get CodeArtifact Auth Token') { 
                    steps { 
                        script {
                            codeArtifact.getAuthorizationToken(
                                exportToEnv: true,
                                domain     : env.JAVA_PACKAGES_CODEARTIFACT_REPOSITORY_DOMAIN,
                                domainOwner: env.AWS_ACCOUNT_ID,
                                region     : env.AWS_CODEARTIFACT_REGION
                            )
                        }
                    } 
                }

                stage('Environment Preparation') { 
                    steps { 
                        script {
                            environmentPreparation(
                                accountId: env.accountId,
                                userId: env.userId
                            )
                        }
                    } 
                }
            }
        }
        stage('Unit Tests') {
            steps {
                script {
                    unitTests(settings: 'settings.xml')
                }
            }
        }
        stage('Static Analysis') {
            parallel {
                stage('Checkstyle') {
                    steps {
                        script {
                            checkstyleReport(
                                settings: 'settings.xml',
                                checkstyleGate: [
                                    threshold: 50,
                                    type: 'TOTAL',
                                    unstable: true
                                ]
                            )
                        }
                    }
                }
                stage('SpotBugs') {
                    steps {
                        script {
                            spotBugsReport(
                                settings: 'settings.xml',
                                spotbugsGate: [
                                    threshold: 20,
                                    type: 'TOTAL',
                                    unstable: true
                                ]
                            )
                        }
                    }
                }
                stage('Jacoco') {
                    steps {
                        script {
                            jacocoReport(
                                settings: 'settings.xml',
                                jacocoGate: [
                                    metric: 'LINE',
                                    threshold: 70.0
                                ]
                            )
                        }
                    }
                }
                stage('SonarQube Branch Analysis') {
                    when { expression { sonarBranchAnalysis.isBranchValid() } }
                    steps {
                        script {
                            sonarBranchAnalysis(
                                settings: 'settings.xml',
                                defaultTarget: 'dev',
                                sonarEnv: 'default_env'
                            )
                        }
                    }
                }
            }
        }

        stage('Semantic Versioning') {
            steps {
                script {
                    semanticVersioning(
                        settings: 'settings.xml',
                        lockPrefix: env.REPOSITORY_NAME,
                        branch: env.BRANCH_NAME,
                        credentialsId: 'github-credentials-recruitcrm-engineering',
                        npmInstallCmd: 'npm ci',
                        semanticCmd: 'npx semantic-release',
                        debug: true
                    )
                }
            }
        }
    }
    post {
        success {
            echo "Finished successfully."
            buildSlackNotify(
                repositoryName: env.REPOSITORY_NAME,
                branch: env.BRANCH_NAME,
                buildStatus: 'SUCCESS'
            )
        }
        failure {
            echo "Finished with failure."
            buildSlackNotify(
                repositoryName: env.REPOSITORY_NAME,
                branch: env.BRANCH_NAME,
                buildStatus: 'FAILURE'
            )
        }
        unstable {
            echo "Finished with unstable."
            buildSlackNotify(
                repositoryName: env.REPOSITORY_NAME,
                branch: env.BRANCH_NAME,
                buildStatus: 'UNSTABLE'
            )
        }
        aborted {
            echo "Pipeline aborted."
            buildSlackNotify(
                repositoryName: env.REPOSITORY_NAME,
                branch: env.BRANCH_NAME,
                buildStatus: 'ABORTED'
            )
        }
        always {
            alwaysBlock()
            publishBuildMetrics()
        }
    }
}