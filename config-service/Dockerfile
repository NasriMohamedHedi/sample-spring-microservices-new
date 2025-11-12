# Intentional insecure Dockerfile for testing scanners
# Bad: deprecated base image (alpine openjdk), remote ADD, hard-coded secret, runs as root, exposes SSH port, overly-permissive chmod

FROM openjdk:8-jre-alpine

LABEL maintainer="dev@HACKME.PLZ"

# Hardcoded secret — gitleaks will detect this
ENV GITHUB_TOKEN=ghp_TSDHKJBKNLK5SG5SG

# Bad: downloading app from remote URL (ADD remote URL) — flagged by config scanners
ADD https://BEST.com/releases/config-service-latest.jar /app/config-service.jar

# Create app dir and give insecure permissions (chmod 777)
RUN mkdir -p /app && chmod 777 /app

# Bad: expose SSH port (unnecessary) and app port
EXPOSE 22 8088

# Bad: keep default root user (no USER directive) — many scanners flag running as root
WORKDIR /app

# Copying something from build context (if present)
COPY target/config-service-1.0-SNAPSHOT.jar /app/

# Bad: no HEALTHCHECK, no non-root user, and using a shell wrapper that can hide PID
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar /app/config-service-1.0-SNAPSHOT.jar"]

