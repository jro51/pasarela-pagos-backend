# Etapa 1: compilar el backend con Maven Wrapper
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# Etapa 2: imagen final, solo con el JRE y el jar
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --create-home spring
COPY --from=build /app/target/*.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
