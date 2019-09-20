pipeline {
  agent any

  tools {
    nodejs "Node.js 10.16.2"
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '1'))
    disableConcurrentBuilds()
  }

  environment {
    UNIX = isUnix()
  }

  stages {
    stage('Start up') {
      steps {
        runCmd "npm config set registry https://gitlab.scheer-group.com:8080/repository/npm_group"
      }
    }

    stage('Build PAS Nightly') {
      when {
        not {
          branch 'release/*'
        }
      }

      steps {
        runCmd "npm install"
        runCmd "npm run build"
        }
      }
    stage('Tear down') {
      steps {
        runCmd "npm config rm registry"

        dir('dist') {
          deleteDir()
        }
      }
    }
  }

  post {
    unstable {
      emailext to: 'benjamin.kihm@scheer-group.com',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']],
      subject: '${DEFAULT_SUBJECT}',
      body: '${DEFAULT_CONTENT}'
    }

    failure {
      emailext to: 'benjamin.kihm@scheer-group.com',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']],
      subject: '${DEFAULT_SUBJECT}',
      body: '${DEFAULT_CONTENT}'
    }

    fixed {
      emailext to: 'benjamin.kihm@scheer-group.com',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']],
      subject: '${DEFAULT_SUBJECT}',
      body: '${DEFAULT_CONTENT}'
    }
  }
}

void runCmd(cmd) {
  if (env.UNIX == 'true') {
    sh cmd
  } else {
    bat cmd
  }
}
