pipeline {
    agent {
        label 'agent-java21'
    }

    parameters {
        string(name: 'ENV_NAME', defaultValue: 'production', description: 'Name of the environment')
        string(name: 'PACKAGE_VERSION', defaultValue: '', description: 'Artifact version to be deployed')
    }


    stages {
        stage('Printenv') {
            steps {
                script {
                    sh 'printenv | sort'
                }
            }
        }

        stage('Run DSL Script') {
            steps {
                script {
                    // Run the DSL script using the jobDsl step
                    jobDsl targets: 'scripts/jenkins/dsl_deploy_production.groovy',
                            additionalParameters: [
                                    ENV_NAME                   : params.ENV_NAME,
                                    PACKAGE_VERSION            : params.PACKAGE_VERSION,
                            ]
                }
            }
        }
    }
}