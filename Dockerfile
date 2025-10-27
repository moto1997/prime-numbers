# Build (Java 20)
FROM eclipse-temurin:20-jdk AS build
WORKDIR /app
COPY . .
# Ensure wrapper is executable on Linux builders
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests package

# Run (Java 20 JRE)
FROM eclipse-temurin:20-jre
WORKDIR /app
# copy the built jar (handles -SNAPSHOT or versioned names)
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
CMD ["sh","-c","java -Dserver.port=$PORT -jar app.jar"]
