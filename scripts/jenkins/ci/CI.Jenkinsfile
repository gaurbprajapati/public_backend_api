@Library('jenkins-shared-library@main') _

pipeline {
    agent {
        kubernetes {
            yaml libraryResource('kubernetes/java-21-ci-agent.yml')
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
        // Dont disable concurrent builds for CI due to sematic versioning
        // disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        throttleJobProperty(
            categories: ['contract-staffing-timesheet-microservice'],
            throttleEnabled: true,
            throttleOption: 'category'
        )
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
        stage('Parallel-1') {
            parallel {
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

                stage('SAST Scan') {
                    steps {
                        script {
                            fluidAttackSAST(
                                container: 'sast',
                                namespace: 'contract-staffing-timesheet-microservice',
                                reportFile: 'Fluid-Attacks-Results.csv',
                                htmlReport: 'sast-report.html',
                                branch: env.BRANCH_NAME,
                                configFile: 'sast-config.yaml'
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
                stage('OWASP Dependency Check') {
                    steps {
                        script {
                            owaspDependencyCheck(
                                suppressionFile: 'dependency-check-suppressions.xml'
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
        stage('Integration Tests') {
            steps {
                lock("integration-tests-${env.REPOSITORY_NAME}-${env.INTEGRATION_TEST_ENV}") {
                    script {
                        try {
                            asgScaling.upscale(
                                asgName: env.ASG_NAME,
                                region: 'ap-south-1',
                                job: 'asg-scaling'
                            )

                            deployVersion(
                                settings: 'settings.xml',
                                serviceName: 'contract-staffing-timesheet',
                                envName: env.INTEGRATION_TEST_ENV,
                                waitSeconds: 30
                            )

                            integrationTest(
                                envName: env.INTEGRATION_TEST_ENV,
                                testSuite: 'contractStaffing',
                                dbName: 'Test',
                                email: env.LAST_COMMIT_EMAIL,
                                jobName: 'test-the-rest-jenkinsfile'
                            )
                        } finally {
                            asgScaling.downscale(
                                asgName: env.ASG_NAME,
                                region: 'ap-south-1',
                                job: 'asg-scaling'
                            )
                        }
                    }
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
            publishCiMetrics()
        }
    }
}