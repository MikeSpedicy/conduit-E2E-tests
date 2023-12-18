pipeline {
    agent any
    // fake changes 2
    // environment {
    //     astraUsername = credentials("${astraUsername}")
    //     astraPassword = credentials("${astraPassword}")
    // }

    stages {
        stage('Build repo and start Frontend') {
            steps {
                // Get some code from a GitHub repository
                // git branch: 'test', credentialsId: '9b052a9c-9e56-40b6-b305-63ff570a1c86', url: 'https://github.com/MikeSpedicy/conduit-E2E-tests'
                checkout scm
                script {
                    def dockerLogFilePath = 'logfileDocker.txt'
                    def frontLogFilePath = 'logfileFront.txt'
                    def dockerComplitionText = "[GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-48"
                    
                    if (!fileExists('kafdrop/docker-compose')) {
                        powershell "git clone https://github.com/obsidiandynamics/kafdrop.git"
                    }
                    dir('kafdrop/docker-compose/kafka-kafdrop') {
                        // bat "start docker init"
                        bat "start /B cmd /C docker-compose up > ${dockerLogFilePath} 2>&1"
                        waitForServerReady(dockerComplitionText, dockerLogFilePath, "Attaching to kafka-kafdrop-kafdrop-1, kafka-kafdrop-kafka-1")
                    }
                    if (!fileExists('node_modules')) {
                        bat "npm install"
                    }
                    bat "start /B cmd /C npx nx serve client > ${frontLogFilePath} 2>&1"
                    waitForServerReady('Compiled successfully.', frontLogFilePath, false)
                    // powershell "cd /; "
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                script {
                    withEnv(["astraUsername = ${astraUsername}", "astraPassword = ${astraPassword}"]) {
                        def logFilePath = 'logfileBack.txt'
                        
                        echo "astraUsername - ${astraUsername}"
                        echo "astraPassword - ${astraPassword}"
                        echo "astraUsername env. - ${env.astraUsername}"
                        echo "astraPassword env. - ${env.astraPassword}"
                        // powershell "$env:astraUsername"
                        bat "start /B cmd /C npx nx run-many --target=serve --projects=auth-service,conduit-gateway,profile-service,article-service --parallel=4 > ${logFilePath} 2>&1"
                        waitForServerReady('Application is running', logFilePath, false)
                        waitForServerReady('No errors found.', logFilePath, false)
                    }
                }
            }
        }
        
        stage('Run tests') {
            tools {
                gradle 'gradle8dot4'
            }
            steps {
                script {
                    dir('E2ETests/SeleniumAutomation/Java') {
                        if (!fileExists('build')) {
                            // bat 'gradlew wrapper --gradle-version 8.4'
                            bat "gradlew.bat build"
                        } else {
                            bat "gradlew.bat test"
                        }
                    }
                }
            }
        }

        // stage('Approve Commit') {
        //     steps {
        //         git 'https://github.com/MikeSpedicy/conduit-E2E-tests.git' {
        //             approve 'Approved by jenkins', onlyIfSuccessful: true
        //         }
        //     }
        // }
        
    }
    post {
        // when success - records the test results and archive the jar file.
        success {
            // Publish the HTML report as a JUnit test report
            // junit allowEmptyResults: true, testResults: 'E2ETests/SeleniumAutomation/Java/build/reports/tests/test/*.xml'

            // Publish the HTML report
            publishHTML (target : [
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'E2ETests/SeleniumAutomation/Java/build/reports/tests/test',
                reportFiles: 'index.html',
                reportName: 'Reports',
                reportTitles: 'Tests Report'
            ])
            script {
                // push changes to the master branch
                def source_branch = 'test'
                def targetBranch = 'master'
                // Fetch and merge changes from the feature branch to the target branch
                // sh "git fetch origin ${source_branch}:${source_branch}"
                sh "git checkout ${targetBranch}"
                sh "git merge ${source_branch}" //  --no-ff --no-commit
                // sh "git commit -m 'Merge changes from ${source_branch}'"
                sh "git push origin ${targetBranch}"
                // powershell "git "
                // git 'https://github.com/MikeSpedicy/conduit-E2E-tests.git' {
                //     approve 'Approved by jenkins'
                // } // later shoul check how to configure git commit approving in Jenkins
                // archiveArtifacts '*.jar'
            }
        }
        always {
            // Stop the running processes
            script {
                cleanUp()
            }
        }
    }
}

def waitForServerReady(logText, logFilePath, alternativeLogText) {
    timeout(time: 3, unit: 'MINUTES') {
        // Continuously check the console output for the log message indicating server readiness
        script {
            def serverReady = false
            def logCheckInterval = 4 // seconds

            // Loop until the server is ready or timeout occurs
            while (!serverReady) {
                echo "Checking ${logText} in the ${logFilePath} for server readiness..."
                serverReady = isServerReady(logText, logFilePath, alternativeLogText)
                sleep logCheckInterval
            }

            if (!serverReady) {
                error 'Process did not indicate readiness within the timeout'
            }
        }
    }
}

def isServerReady(logText, logFilePath, alternativeLogText) {
    def logContent = readFile(file: logFilePath).trim()
    echo "log content - ${logContent}"
    def valueToReturn = false
    if (logContent.contains(logText) || (alternativeLogText ? logContent.contains(alternativeLogText) : false)) {
        valueToReturn = true
    }
    return valueToReturn
}

def cleanUp() {
    echo 'Performing cleanup...'
    dir('kafdrop/docker-compose/kafka-kafdrop') {
        bat("start docker-compose stop")
    }
    // bat 'Stop-Process -CommandLine "*npx nx serve client*" -Force'
    // bat 'Stop-Process -CommandLine "*npx nx run-many*" -Force'
}