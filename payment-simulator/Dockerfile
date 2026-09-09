FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copia o JAR que o Gradle da máquina real vai gerar
COPY build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
