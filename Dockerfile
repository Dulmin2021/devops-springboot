# ─────────────────────────────────────────────────────────────────────────────
# Dockerfile — Multi-step: build context assumed to already contain target/*.jar
# (Maven is run by Jenkins before docker build).
#
# Base image: eclipse-temurin:21-jre
#   → Provides a minimal JRE 21 runtime without the full JDK.
#   → eclipse-temurin is the official OpenJDK distribution from Adoptium.
# ─────────────────────────────────────────────────────────────────────────────

FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the fat-jar produced by Maven into the image.
# The wildcard picks up whatever version number is in the filename.
COPY target/*.jar app.jar

# Document that the application listens on 8080
EXPOSE 8080

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]
