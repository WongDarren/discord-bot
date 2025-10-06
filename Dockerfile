# Build stage with Maven and Java 24
FROM maven:3.9.11-amazoncorretto-24 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Runtime stage
FROM amazoncorretto:24
WORKDIR /app
COPY --from=build /app/target/discordbot.jar .
CMD ["java", "-jar", "discordbot.jar"]