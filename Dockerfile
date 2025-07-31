# Start with a base image containing Java runtime (version 17 to match the POM file)
FROM amazoncorretto:17-alpine-jdk as build
# Add Maintainer Info
LABEL maintainer="your-email@example.com"

# Set the current working directory inside the image
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Give execution rights on the Maven wrapper
RUN chmod +x mvnw

# Copy the pom.xml file
COPY pom.xml .

# Copy the project source
COPY src src

# Package the application
RUN ./mvnw package -DskipTests

# Run stage
FROM amazoncorretto:17-alpine-jdk

COPY --from=build /app/target/*.jar app.jar


RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

COPY .env .
RUN cat .env
RUN ls -ahl

# Make port 8084 available to the world outside this container
EXPOSE 9001

# Run the jar file
CMD ["java", "-jar", "app.jar"]


