folder("${ENV_NAME}_pipeline_jobs") {
    displayName("${ENV_NAME}")
    description("Service pipelines for ${ENV_NAME} environment")
}

pipelineJob("${ENV_NAME}_pipeline_jobs/${ENV_NAME}_contract-staffing-timesheet") {
    description('Pipeline job for the Contract Staffing Timesheet service')
    parameters {
        stringParam('ENV_NAME', "${ENV_NAME}", 'Environment name')
        gitParameterDefinition {
            name('PACKAGE_VERSION')
            description('Artifact version to be deployed')
            type('PT_TAG')
            defaultValue('main')
            branch('*')
            branchFilter('.*')
            tagFilter('*')
            quickFilterEnabled(true)
            selectedValue('NONE')
            sortMode('DESCENDING_SMART') // Sorting: NONE, ASCENDING, DESCENDING, ASCENDING_SMART, DESCENDING_SMART
            useRepository('https://github.com/Workforce-Cloud-Tech/contract-staffing-timesheet-microservice.git')
        }
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/Workforce-Cloud-Tech/contract-staffing-timesheet-microservice.git')
                        credentials('github-credentials-recruitcrm-engineering')
                    }
                    branch('${PACKAGE_VERSION}')
                    extensions {
                        localBranch()
                    }
                }
            }
            scriptPath('scripts/jenkins/DeployProduction.Jenkinsfile')
        }
    }
}