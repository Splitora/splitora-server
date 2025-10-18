# Use an official OpenJDK runtime as the base image
FROM eclipse-temurin:17-jdk-ubi9-minimal

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file into the container at /app
COPY target/*.jar app.jar

# Accept build-time arguments for spring profile
ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# Expose the port the app runs in
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]