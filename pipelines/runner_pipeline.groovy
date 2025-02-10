timeout(time: 10, unit: 'MINUTES') {
    node('gradle') {

        if (!params.CONFIG?.trim()) {
            error("CONFIG parameter is empty or invalid!")
        }

        def config = readYaml text: params.CONFIG
        echo "Loaded configuration: ${config}"

        config.each { k, v ->
            env."${k}" = v
        }

        if (!env.TESTS?.trim()) {
            error("TESTS environment variable is not defined or empty!")
        }

        def jobs = [:]

        echo "TESTS: ${env.TESTS}"

        for (def test_type : env.TESTS.split(",")) {
            def current_test = test_type.trim()

            jobs[current_test] = {
                node('gradle') {
                    stage("Running $current_test tests") {
                        echo "Running tests for: $current_test"
                        def parameters = [
                                string(name: 'REFSPEC', value: params.REFSPEC ?: "main")
                        ]
                        build(job: "${current_test}-tests", parameters: parameters, propagate: true)
                    }
                }
            }
        }

        parallel jobs
    }

    node('gradle') {
        stage('Collect Allure Results') {
            sh 'mkdir -p allure-results'

            copyArtifacts(
                    projectName: 'ui-tests',
                    filter: 'build/allure-results/**',
                    target: 'allure-results',
                    flatten: true,
                    allowEmpty: true
            )
            copyArtifacts(
                    projectName: 'api-tests',
                    filter: 'build/allure-results/**',
                    target: 'allure-results',
                    flatten: true,
                    allowEmpty: true
            )
        }

        stage('Generate Allure Report') {
            sh 'ls -lah allure-results'  // Проверка наличия файлов перед генерацией отчёта
            allure([
                    results: [[path: 'allure-results']]
            ])
        }
    }
}
