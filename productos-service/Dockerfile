# Etapa 1: Construcción con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con solo el JAR
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar productos.jar
ENTRYPOINT ["java", "-jar", "productos.jar"]
