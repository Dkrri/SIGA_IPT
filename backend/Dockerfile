# ------------------------------------------------------------------
# Build stage: compile the Spring Boot jar with Maven Wrapper
# ------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Cache Maven dependencies separately from the source code.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -Dmaven.test.skip=true dependency:go-offline

# Compile the application.
COPY src/ src/
RUN ./mvnw -B -Dmaven.test.skip=true package

# ------------------------------------------------------------------
# Runtime stage: only the JRE and the packaged jar
# ------------------------------------------------------------------
FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system siga && useradd --system --gid siga siga

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER siga

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:8080/api/public/ping || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
