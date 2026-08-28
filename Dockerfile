# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Camada de dependências separada: mudar código não refaz o download.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Container não roda como root. Vários hosts recusam se rodar.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /build/target/*.jar app.jar
USER app

EXPOSE 8080

# MaxRAMPercentage é o que evita o OOM killer em máquina de 1 GB:
# sem isso a JVM dimensiona o heap achando que a memória do host é dela.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
