#!/usr/bin/env groovy

@Library('jenkins-jira-integration@dev') _

pipeline {

  agent {
    node {
      label 'cscabbia'
      customWorkspace "workspace/Api-Mgmt-Dev-Portal-Pipeline"
    }
  }

  tools {
    nodejs "10"
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '1'))
    disableConcurrentBuilds()
    lock('ApiMgmt-Devportal-Build')
  }

  environment {
    // Snippet taken from https://gist.github.com/DarrenN/8c6a5b969481725a4413
    PACKAGE_VERSION = sh(script: 'cat package.json | grep version | head -1 | awk -F= "{ print $2 }" | sed \'s/[version:,\",]//g\' | tr -d \'[[:space:]]\'', returnStdout: true)
    // Snippet taken from https://issues.jenkins-ci.org/browse/JENKINS-44449
    GIT_COMMIT_SHORT = sh(script: "printf \$(git rev-parse --short=7 ${GIT_COMMIT})", returnStdout: true)
  }

  stages {
    stage('Start up') {
      steps {
        sh "npm config set registry https://gitlab.scheer-group.com:8080/repository/npm_group"
      }
    }

    stage('Clean') {
      steps {
        sh 'git clean -xdf'
      }
    }

    stage('Build devportal') {
      steps {
        sh "npm install"
        sh "npm run build"
      }
    }

    stage('Prepare base docker image') {
      steps {
        sh """
          docker build -t devportal-base:latest -f ./docker/images/base/Dockerfile .
          docker build -t devportal-tests:latest -f ./docker/images/test/Dockerfile .
        """
      }
    }

    stage('Tests') {
      agent {
        docker {
          image 'devportal-tests:latest'
          label 'docker'
          args '-u root:root'
        }
      }
      steps {
        sh "cd '/usr/src/app/' && npm run test"
        /* copy test result into workspace destination */
        sh "cp -r /usr/src/app/junit/* ./junit"
      }
      post {
        always {
          /* collect the test results */
          junit "junit/api-mgmt-dev-portal/*.xml*"
        }
      }
    }

    stage('Build docker image') {
      steps {
        sh """
          docker build -t api-mgmt/devportal:${PACKAGE_VERSION} .
          docker image save api-mgmt/devportal:${PACKAGE_VERSION} -o api-mgmt-devportal-${PACKAGE_VERSION}-overlay.tar
        """
      }
    }

    stage('Archive builds') {
      when {
        not {
          branch '**/e2e_release'
        }
      }
      steps {
        sh "rename.ul overlay ${GIT_COMMIT_SHORT} *.tar"
        archiveArtifacts artifacts: '*.tar'
      }
    }

    stage('Archive release builds') {
      when {
        anyOf {
          branch '**/e2e_release'
        }
      }
      steps {
        sh 'rename.ul -- "-overlay" "" *.tar'
        archiveArtifacts artifacts: '*.tar'
      }
    }


    stage('Publish nightly builds to Nexus') {
        when {
            anyOf {
                branch '**/publish-nightly'
            }
        }
        steps {
          withDockerRegistry([credentialsId: 'nexus', url: "https://gitlab.scheer-group.com:8080"]) {
            sh './ci/publish-images.sh ${PACKAGE_VERSION} nightly'
          }
        }
    }

    stage('Publish master nightly builds to NAS1/Nexus') {
      when {
        anyOf {
          branch '**/e2e_master'
        }
      }
      steps {
        cifsPublisher alwaysPublishFromMaster: false, continueOnError: false, failOnError: false,
          paramPublish: null, masterNodeName: '',
          publishers: [[configName: 'NAS1', transfers:
              [
                [sourceFiles    : '*.tar',
                 removePrefix   : '',
                 remoteDirectory: "api-mgmt/nightlyBuilds/${PACKAGE_VERSION}-${GIT_COMMIT_SHORT}"]
              ]
           ]]

        withDockerRegistry([credentialsId: 'nexus', url: "https://gitlab.scheer-group.com:8080"]) {
          sh './ci/publish-images.sh ${PACKAGE_VERSION} latest'
        }
      }
    }

    stage('Publish release builds to NAS1/Nexus') {
      when {
        anyOf {
          branch '**/e2e_release'
        }
      }
      steps {
        cifsPublisher alwaysPublishFromMaster: false, continueOnError: false, failOnError: false,
          paramPublish: null, masterNodeName: '',
          publishers: [[configName: 'NAS1', transfers:
              [
                [sourceFiles    : '*.tar',
                 removePrefix   : '',
                 remoteDirectory: "api-mgmt/${PACKAGE_VERSION}"]
              ]
           ]]

        withDockerRegistry([credentialsId: 'nexus', url: "https://gitlab.scheer-group.com:8080"]) {
          sh './ci/publish-images.sh ${PACKAGE_VERSION} release'
        }
      }
    }

    stage('Remove container image') {
      steps {
        sh """
          docker image rm -f api-mgmt/devportal:${PACKAGE_VERSION} || true
          docker image rm devportal-tests:latest || true
          docker image rm devportal-base:latest || true
          """
      }
    }

    stage('Tear down') {
      steps {
        sh "npm config rm registry"
      }
    }
  }

  post {
    always {
      script {
        jenkinsJiraIntegration(['JiraSiteName': 'Jira'])
      }
    }
    aborted {
      emailext to: 'benjamin.kihm@scheer-group.com, florian.volk@scheer-group.com',
        recipientProviders: [[$class: 'CulpritsRecipientProvider']],
        subject: '${DEFAULT_SUBJECT}',
        body: '${DEFAULT_CONTENT}'
    }
    unstable {
      emailext to: 'benjamin.kihm@scheer-group.com, florian.volk@scheer-group.com',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']],
      subject: '${DEFAULT_SUBJECT}',
      body: '${DEFAULT_CONTENT}'
    }

    failure {
      emailext to: 'benjamin.kihm@scheer-group.com, florian.volk@scheer-group.com',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']],
      subject: '${DEFAULT_SUBJECT}',
      body: '${DEFAULT_CONTENT}'
    }

    fixed {
      emailext to: 'benjamin.kihm@scheer-group.com, florian.volk@scheer-group.com',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']],
      subject: '${DEFAULT_SUBJECT}',
      body: '${DEFAULT_CONTENT}'
    }
  }
}
