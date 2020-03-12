final String CI_NAME = "PretronicCI"
final String CI_EMAIL = "ci@pretronic.net"
final String COMMIT_MESSAGE = "Version change %version%"
final String RESOURCE_ID = "0249f842-de95-42df-b611-7ad390d90086"

final String BRANCH_DEVELOPMENT = "origin/development"
final String BRANCH_BETA = "origin/beta"
final String BRANCH_MASTER = "origin/master"
final String PROJECT_SSH = "git@github.com:Fridious/DKCoins.git"

String PROJECT_NAME = "DKCoins"
String OLD_VERSION = "UNDEFINED"
String BRANCH = "UNDEFINED"
boolean SKIP = false

String QUALIFIER = "UNDEFINDED"

pipeline {
    agent any
    tools {
        maven 'Maven3'
        jdk 'Java8'
    }
    options {
        buildDiscarder logRotator(numToKeepStr: '10')
    }
    stages {
        stage('CI Check') {
            steps {
                script {
                    String name = sh script: 'git log -1 --pretty=format:\"%an\"', returnStdout: true
                    String email = sh script: 'git log -1 --pretty=format:\"%ae\"', returnStdout: true
                    if (name == CI_NAME && email == CI_EMAIL) {
                        SKIP = true;
                    }
                }
            }
        }
        stage('Read information') {
            when { equals expected: false, actual: SKIP }
            steps {
                script {
                    OLD_VERSION = readMavenPom().getVersion()
                    BRANCH = env.GIT_BRANCH
                }
            }
        }
        stage('Version change') {
            when { equals expected: false, actual: SKIP }
            steps {
                script {
                    String[] versionSplit = OLD_VERSION.split("[-.]")

                    String major = versionSplit[0]
                    int minorVersion = versionSplit[1].toInteger()
                    int patchVersion = versionSplit[2].toInteger()

                    if(BRANCH.equalsIgnoreCase(BRANCH_MASTER)) {
                        OLD_VERSION = major + "." + minorVersion + "." + patchVersion
                    } else if (BRANCH.equalsIgnoreCase(BRANCH_DEVELOPMENT)) {
                        if (!OLD_VERSION.endsWith("-SNAPSHOT")) {
                            OLD_VERSION = OLD_VERSION + '-SNAPSHOT'
                        }
                    } else if (BRANCH.equalsIgnoreCase(BRANCH_BETA)) {
                        if (!OLD_VERSION.endsWith("-BETA")) {
                            OLD_VERSION = OLD_VERSION + '-BETA'
                        }
                    }
                    sh "mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$OLD_VERSION"
                }
            }
        }

        stage('Build and deploy') {
            when { equals expected: false, actual: SKIP }
            steps {
                configFileProvider([configFile(fileId: 'afe25550-309e-40c1-80ad-59da7989fb4e', variable: 'MAVEN_GLOBAL_SETTINGS')]) {
                    sh 'mvn -B -gs $MAVEN_GLOBAL_SETTINGS clean deploy'
                }
            }
        }
        stage('Archive') {
            when { equals expected: false, actual: SKIP }
            steps {
                archiveArtifacts artifacts: '**/target/*.jar'
            }
        }
    }
    post {
        success {
            script {
                if(!SKIP) {
                    sh """
                    git config --global user.name '$CI_NAME' -v
                    git config --global user.email '$CI_EMAIL' -v
                    """

                    String[] versionSplit = OLD_VERSION.split("[-.]")

                    String major = versionSplit[0]
                    int minorVersion = versionSplit[1].toInteger()
                    int patchVersion = versionSplit[2].toInteger()

                    if (BRANCH == BRANCH_DEVELOPMENT) {
                        QUALIFIER = "SNAPSHOT"
                        patchVersion++

                        String version = major + "." + minorVersion + "." + patchVersion + "-SNAPSHOT"
                        String commitMessage = COMMIT_MESSAGE.replace("%version%", version)
                        sh """
                        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$version
                        git add . -v
                        git commit -m '$commitMessage' -v
                        """

                        sshagent(['1c1bd183-26c9-48aa-94ab-3fe4f0bb39ae']) {
                            sh "git push origin HEAD:development -v"
                        }
                    } else if(BRANCH == BRANCH_BETA) {
                        QUALIFIER = "BETA"
                        String version = major + "." + minorVersion + "." + patchVersion + "-BETA"

                        String commitMessage = COMMIT_MESSAGE.replace("%version%", version)
                        sshagent(['1c1bd183-26c9-48aa-94ab-3fe4f0bb39ae']) {

                            sh """
                            mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$version
                            git add . -v
                            git commit -m '$commitMessage-BETA' -v
                            git push origin HEAD:beta -v
                            """
                            minorVersion++
                            patchVersion = 0

                            version = major + "." + minorVersion + "." + patchVersion + "-SNAPSHOT"

                            commitMessage = COMMIT_MESSAGE.replace("%version%", version)

                            sh """
                            if [ -d "tempDevelopment" ]; then rm -Rf tempDevelopment; fi
                            mkdir tempDevelopment
                            cd tempDevelopment/
                            git clone --single-branch --branch development $PROJECT_SSH

                            cd $PROJECT_NAME/
                            mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$version-SNAPSHOT

                            git add . -v
                            git commit -m '$commitMessage-SNAPSHOT' -v
                            git push origin HEAD:development -v
                            cd ..
                            cd ..
                            if [ -d "tempDevelopment" ]; then rm -Rf tempDevelopment; fi

                            """
                        }
                    } else if (BRANCH == BRANCH_MASTER) {
                        QUALIFIER = "STABLE"
                        String version = major + "." + minorVersion + "." + patchVersion

                        String commitMessage = COMMIT_MESSAGE.replace("%version%", version)

                        sshagent(['1c1bd183-26c9-48aa-94ab-3fe4f0bb39ae']) {
                            sh """
                            mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$version
                            git add . -v
                            git commit -m '$commitMessage' -v
                            git push origin HEAD:master -v
                            """
                        }
                    }

                    withCredentials([string(credentialsId: '120a9a64-81a7-4557-80bf-161e3ab8b976', variable: 'SECRET')]) {
                        int buildNumber = env.BUILD_NUMBER;
                        httpRequest(acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON',
                                httpMode: 'POST', ignoreSslErrors: true,timeout: 3000,
                                responseHandle: 'NONE',
                                customHeaders:[[name:'token', value:"${SECRET}", maskValue:true]],
                                url: "https://mirror.pretronic.net/v1/$RESOURCE_ID/versions/create?name=$OLD_VERSION&qualifier=$QUALIFIER&buildNumber=$buildNumber")

                        httpRequest(acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_OCTETSTREAM',
                                httpMode: 'POST', ignoreSslErrors: true, timeout: 3000,
                                multipartName: 'file',
                                responseHandle: 'NONE',
                                uploadFile: "dkcoins-minecraft/target/dkcoins-minecraft-${OLD_VERSION}.jar",
                                customHeaders:[[name:'token', value:"${SECRET}", maskValue:true]],
                                url: "https://mirror.pretronic.net/v1/$RESOURCE_ID/versions/$buildNumber/publish?edition=default")
                    }
                }
            }
        }
    }
}

