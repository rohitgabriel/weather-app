pipeline {
    agent any
    environment {
        AZURE_CRED=credentials('AZURE_CRED')
        AZURE_ID=credentials('AZURE_ID')
        AZUREPUB=credentials('AZUREPUB')
        client_id="${AZURE_CRED_USR}"
        client_secret="${AZURE_CRED_PSW}"
        tenant_id="${AZURE_ID_USR}"
        subscription_id="${AZURE_ID_PSW}"
        location="australiaeast"
    }
    stages {
        // stage('Checkout repo') {
        //     steps {
        //         git branch: 'master',
        //         credentialsId: 'mygitcredid',
        //         url: 'https://github.com/rohitgabriel/aws-deployment.git'
        //     }
        // }
        // stage ('Deploy') {
        //     steps{
        //         sshagent(credentials : ['awskey']) {
        //         sh 'ssh -o StrictHostKeyChecking=no ubuntu@13.54.226.2 uptime'
        //         sh 'ssh -v ubuntu@13.54.226.2'
        //         sh 'scp ./get-instance-id.sh ubuntu@13.54.226.2:/tmp/target'
        //         }
        //     }
        // }
        stage("Get Instance IP and setup script") {
            steps {
                script {
                    instanceIP = sh(script: './getazure-instance-id.sh', returnStdout: true).trim()
                }
                
                sshagent(credentials : ['azurekey']) {
                    sh "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no azureuser@${instanceIP} uptime"
                    sh "scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ./deploycode.sh azureuser@${instanceIP}:/tmp/deploycode.sh"
                    sh "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no azureuser@${instanceIP} chmod 755 /tmp/deploycode.sh"
                }
            }
        }
        stage("Build and Deploy WeatherApp") {
            steps {
                sshagent(credentials : ['azurekey']) {
                    sh "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no azureuser@${instanceIP} /tmp/deploycode.sh"
                }
            }
        }
    }
}
