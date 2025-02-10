timeout(time: 5, unit: 'MINUTES') {
    node('python') {
        stage('checkout') {
            checkout scm
        }

        stage('create config') {
            sh "cd jobs_ini"
            sh """cat <<EOF> ./job.ini
                [job_builder]
                ignore_cache=True
                keep_descriptions=False
                recursive=True
                
                [jenkins]
                user=admin
                password=admin
                url=https://localhost:8080/jenkins/
                timeout=30
                EOF"""
        }

        stage('update jobs') {
            sh "jenkins-jobs --conf ./job.ini update ."
        }
    }
}