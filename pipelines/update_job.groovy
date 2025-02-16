timeout(time: 5, unit: 'MINUTES') {
    node('python') {
        stage('checkout') {
            checkout scm
        }

        dir('jobs_ini') {  // Устанавливаем `jobs_ini` как рабочую директорию
            stage('create config') {
                sh '''
                    cat <<EOF > job.ini
[job_builder]
ignore_cache=True
keep_descriptions=False
recursive=True

[jenkins]
user=admin
password=admin
url=http://127.0.0.1:8080/jenkins/
timeout=30
EOF
                '''
            }

            stage('update jobs') {
                sh 'jenkins-jobs --conf job.ini update .'
            }
        }
    }
}