# Use a supported OpenJDK 8 image
FROM eclipse-temurin:8-jre-alpine

# Set environment variables
ENV APP_FILE=config-service-1.0-SNAPSHOT.jar
ENV APP_HOME=/app

# Expose the application port
EXPOSE 8088

# Copy the jar file into the container
COPY target/$APP_FILE $APP_HOME/

# Set the working directory
WORKDIR $APP_HOME

# Run the jar
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]
