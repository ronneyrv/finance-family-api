# ==========================
# Stage 1 - Build
# ==========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# Baixa dependências e cria cache
RUN ./gradlew dependencies --no-daemon

# Agora copia apenas o código
COPY src src

RUN ./gradlew bootJar --no-daemon

# ==========================
# Stage 2 - Runtime
# ==========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/finance.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]