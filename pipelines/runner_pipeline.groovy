timeout(time: 10, unit: 'MINUTES') {
    node('gradle') {

        def config = readYaml text: params.YAML_CONFIG
        config.each {
            k, v -> env."${k}" = v
        }

        def jobs = [:]

        for (def test_type : env.TESTS) {
            jobs[test_type] = node('gradle') {
                stage("Running $test_type tests") {
                    def parameters = [
                            "$REFSPEC",
                            "$YAML_CONFIG"
                    ]

                    build(job: "$test_type-tests",
                            parameters: parameters, propagate: true
                    )
                }
            }
        }

        parallel jobs
    }
}