// ─────────────────────────────────────────────────────────────────────────────
// Jenkinsfile — Declarative CI/CD Pipeline
//
// Stages:
//   1. Checkout    → Pull latest code from GitHub
//   2. Build       → Compile and package the JAR with Maven
//   3. Test        → Run unit tests with Maven
//   4. Docker Build→ Build the Docker image from the Dockerfile
//   5. Deploy      → Stop old container, start new one
//
// Prerequisites on the Jenkins agent:
//   - Java 21 installed (JAVA_HOME set)
//   - Maven 3.x installed (MAVEN_HOME set, or use mvnw wrapper)
//   - Docker installed and Jenkins user added to the 'docker' group
// ─────────────────────────────────────────────────────────────────────────────

pipeline {

    // Run on any available Jenkins agent
    agent any

    // ── Environment variables ────────────────────────────────────────────────
    environment {
        // Docker image name and tag
        IMAGE_NAME = "springboot-devops"
        IMAGE_TAG  = "latest"

        // Container name used for stop / remove / run commands
        CONTAINER_NAME = "springboot-app"

        // Port the Spring Boot container exposes
        APP_PORT = "8080"
    }

    // ── Pipeline stages ──────────────────────────────────────────────────────
    stages {

        // ── Stage 1: Checkout ────────────────────────────────────────────────
        stage("Checkout") {
            steps {
                echo "========== Stage 1: Checkout =========="

                // Jenkins pulls the code from the SCM configured in the job.
                // When triggered by a GitHub webhook this is the pushed commit.
                checkout scm

                echo "Code checkout complete."
            }
        }

        // ── Stage 2: Build ───────────────────────────────────────────────────
        stage("Build") {
            steps {
                echo "========== Stage 2: Build =========="

                // -DskipTests skips test execution here; tests run in Stage 3.
                // clean package removes old build artifacts before compiling.
                sh "mvn clean package -DskipTests"

                echo "Build complete. JAR is in target/"
            }
        }

        // ── Stage 3: Test ────────────────────────────────────────────────────
        stage("Test") {
            steps {
                echo "========== Stage 3: Test =========="

                // Run the full test suite. Jenkins will mark the build FAILED
                // if any test fails, preventing a broken image from being deployed.
                sh "mvn test"

                echo "All tests passed."
            }

            // Publish JUnit XML test results to the Jenkins UI
            post {
                always {
                    junit "target/surefire-reports/*.xml"
                }
            }
        }

        // ── Stage 4: Docker Build ────────────────────────────────────────────
        stage("Docker Build") {
            steps {
                echo "========== Stage 4: Docker Build =========="

                // Build the Docker image using the Dockerfile in the repo root.
                // The image is tagged as springboot-devops:latest.
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."

                echo "Docker image built: ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        // ── Stage 5: Deploy ──────────────────────────────────────────────────
        stage("Deploy") {
            steps {
                echo "========== Stage 5: Deploy =========="

                // Stop the running container if one exists.
                // The '|| true' prevents the pipeline from failing when there
                // is no container to stop (e.g. first deployment).
                sh "docker stop ${CONTAINER_NAME} || true"

                // Remove the old container so we can reuse the same name.
                sh "docker rm ${CONTAINER_NAME} || true"

                // Start the new container in detached mode.
                //   --name         → Give the container a predictable name
                //   -d             → Run in background
                //   -p 8080:8080   → Map host port to container port
                //   --restart      → Restart automatically on crash / reboot
                sh """
                    docker run \\
                        --name ${CONTAINER_NAME} \\
                        -d \\
                        -p ${APP_PORT}:${APP_PORT} \\
                        --restart unless-stopped \\
                        ${IMAGE_NAME}:${IMAGE_TAG}
                """

                echo "Deployment complete. Container '${CONTAINER_NAME}' is running."
            }
        }
    }

    // ── Post-pipeline actions ────────────────────────────────────────────────
    post {

        success {
            echo "✅ Pipeline succeeded! Application deployed successfully."
        }

        failure {
            echo "❌ Pipeline failed. Check the stage logs above for details."
        }

        always {
            echo "Pipeline finished. Cleaning up Jenkins workspace..."
            // Free disk space on the agent after each run
            cleanWs()
        }
    }
}
