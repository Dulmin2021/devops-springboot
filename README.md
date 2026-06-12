# 🚀 springboot-devops

A **beginner-friendly DevOps project** that demonstrates a complete CI/CD pipeline using:

| Technology | Role |
|---|---|
| **Java 21 + Spring Boot** | REST API application |
| **Maven** | Build & dependency management |
| **Docker** | Containerisation |
| **Jenkins** | CI/CD automation |
| **Nginx** | Reverse proxy |
| **GitHub** | Source control + webhook trigger |

---

## 📐 Architecture Diagram

```
  Developer
     │
     │  git push
     ▼
 ┌─────────┐        Webhook         ┌─────────────────────────┐
 │  GitHub │ ──────────────────────▶│        Jenkins          │
 └─────────┘                        │                         │
                                    │  Stage 1: Checkout      │
                                    │  Stage 2: mvn package   │
                                    │  Stage 3: mvn test      │
                                    │  Stage 4: docker build  │
                                    │  Stage 5: docker run    │
                                    └────────────┬────────────┘
                                                 │
                                    Deploys container
                                                 │
                                                 ▼
                                    ┌────────────────────────┐
                                    │      Docker Host       │
                                    │                        │
                                    │  ┌──────────────────┐  │
                              :80 ──┼─▶│   Nginx Proxy    │  │
                                    │  └────────┬─────────┘  │
                                    │           │ :8080       │
                                    │  ┌────────▼─────────┐  │
                                    │  │  Spring Boot App  │  │
                                    │  └──────────────────┘  │
                                    └────────────────────────┘
```

---

## 📂 Project Structure

```
springboot-devops/
│
├── src/
│   ├── main/
│   │   ├── java/com/dulmin/cicd_demo/
│   │   │   ├── DemoApp.java           ← Spring Boot entry point
│   │   │   └── DemoController.java    ← REST endpoints
│   │   └── resources/
│   │       └── application.yml        ← App configuration
│   └── test/
│       └── java/com/dulmin/cicd_demo/
│           └── DemoControllerTest.java ← MockMvc tests
│
├── Dockerfile                          ← Container image definition
├── Jenkinsfile                         ← CI/CD pipeline definition
├── nginx.conf                          ← Reverse proxy configuration
├── docker-compose.yml                  ← Multi-container orchestration
├── .gitignore
└── pom.xml                             ← Maven build configuration
```

---

## 🔌 API Endpoints

| Method | Endpoint  | Response |
|--------|-----------|----------|
| GET    | `/hello`  | `Hello from Spring Boot CI/CD Demo!` |
| GET    | `/health` | `Application is running` |

---

## 🏗️ Build Instructions

### Prerequisites

```bash
# Verify Java 21
java -version

# Verify Maven 3.x
mvn -version

# Verify Docker
docker -version
```

### 1 — Build the JAR locally

```bash
cd springboot-devops
mvn clean package
```

The fat-jar will be created at `target/cicd_demo-1.0.0.jar`.

### 2 — Run tests

```bash
mvn test
```

### 3 — Run locally (without Docker)

```bash
java -jar target/cicd_demo-1.0.0.jar
```

Endpoints are available at `http://localhost:8080/hello` and `http://localhost:8080/health`.

---

## 🐳 Docker Commands

### Build the image manually

```bash
# Build must run after mvn package so target/*.jar exists
mvn clean package -DskipTests
docker build -t springboot-devops:latest .
```

### Run the container alone

```bash
docker run -d \
  --name springboot-app \
  -p 8080:8080 \
  --restart unless-stopped \
  springboot-devops:latest
```

### Start everything with Docker Compose (app + Nginx)

```bash
docker compose up --build -d
```

### Stop all services

```bash
docker compose down
```

### Useful Docker commands

```bash
# View running containers
docker ps

# Stream application logs
docker logs -f springboot-app

# Stop a specific container
docker stop springboot-app

# Remove a specific container
docker rm springboot-app

# List all local images
docker images
```

---

## 🔧 Jenkins Setup

### 1 — Install Jenkins on Ubuntu

```bash
# Add the Jenkins repository
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key \
  | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null

echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/ \
  | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

sudo apt update
sudo apt install -y jenkins

# Start and enable Jenkins
sudo systemctl enable --now jenkins
```

Jenkins is now available at `http://<server-ip>:8080`.

### 2 — Unlock Jenkins

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

Paste the password into the Jenkins web UI to complete setup.

### 3 — Allow Jenkins to run Docker

```bash
# Add the jenkins user to the docker group
sudo usermod -aG docker jenkins

# Restart Jenkins to apply the group change
sudo systemctl restart jenkins
```

### 4 — Install required Jenkins plugins

In **Manage Jenkins → Plugins**, install:

- ✅ Git Plugin
- ✅ Pipeline Plugin
- ✅ GitHub Plugin
- ✅ JUnit Plugin (for test result publishing)
- ✅ Workspace Cleanup Plugin

### 5 — Create a Pipeline Job

1. Click **New Item → Pipeline**
2. Name it `springboot-devops`
3. Under **Pipeline**:
   - Select **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/<your-username>/springboot-devops.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
4. Click **Save**

---

## 🔗 GitHub Webhook Setup

A webhook tells GitHub to notify Jenkins whenever you push code, triggering the pipeline automatically.

### Step 1 — Enable Build Trigger in Jenkins

In the job configuration, check:
> ✅ GitHub hook trigger for GITScm polling

### Step 2 — Add Webhook in GitHub

1. Go to your GitHub repo → **Settings → Webhooks → Add webhook**
2. Fill in:
   | Field | Value |
   |---|---|
   | Payload URL | `http://<your-jenkins-server-ip>:8080/github-webhook/` |
   | Content type | `application/json` |
   | Events | **Just the push event** |
3. Click **Add webhook**

> **Note:** Jenkins must be publicly reachable from GitHub. If running locally, use [ngrok](https://ngrok.com) to expose it temporarily:
> ```bash
> ngrok http 8080
> ```

---

## 🌐 Nginx Configuration Explained

```
nginx.conf
│
├── worker_processes auto          → One worker per CPU core
├── events / worker_connections    → 1024 concurrent connections per worker
└── http
    ├── upstream springboot_backend
    │     └── server springboot-app:8080   ← Docker service name (DNS resolved)
    └── server
          ├── listen 80                    ← Accept traffic on port 80
          ├── server_name localhost
          └── location /
                ├── proxy_pass             → Forward to Spring Boot
                ├── proxy_set_header Host              → Preserve original host
                ├── proxy_set_header X-Real-IP         → Pass real client IP
                ├── proxy_set_header X-Forwarded-For   → Proxy chain
                └── proxy_set_header X-Forwarded-Proto → Original protocol
```

**Why use Nginx?**

- Clients connect to the standard HTTP port (80) instead of 8080
- Nginx can terminate SSL/TLS, so the Java app only handles HTTP internally
- Nginx can serve static files, handle rate limiting and caching
- Decouples the application port from the publicly visible port

---

## ♻️ CI/CD Flow Explained

```
git push origin main
       │
       ▼
GitHub sends webhook POST to Jenkins
       │
       ▼
Jenkins Pipeline triggered
       │
       ├─ Stage 1: Checkout
       │    └── git clone / pull latest code
       │
       ├─ Stage 2: Build
       │    └── mvn clean package -DskipTests
       │         └── Produces target/cicd_demo-1.0.0.jar
       │
       ├─ Stage 3: Test
       │    └── mvn test
       │         └── Runs MockMvc integration tests
       │         └── Publishes JUnit XML report to Jenkins UI
       │         └── ❌ Stops pipeline if any test fails
       │
       ├─ Stage 4: Docker Build
       │    └── docker build -t springboot-devops:latest .
       │         └── Copies JAR into eclipse-temurin:21-jre image
       │
       └─ Stage 5: Deploy
            ├── docker stop springboot-app  (graceful shutdown)
            ├── docker rm   springboot-app  (remove old container)
            └── docker run  springboot-app  (start new container)
                 └── Application live on :8080
                      └── Nginx proxies :80 → :8080
```

Every push to `main` automatically goes through all five stages. A broken test or failed build stops the pipeline before deployment — protecting your production environment.

---

## 🧹 Useful Tips

```bash
# Rebuild everything from scratch (useful after major changes)
docker compose down
docker rmi springboot-devops:latest
mvn clean package
docker compose up --build -d

# Tail logs for both services
docker compose logs -f

# Check which port a container is listening on
docker inspect --format='{{json .NetworkSettings.Ports}}' springboot-app
```

---

## 📜 License

This project is for educational purposes. Feel free to fork and adapt it.
