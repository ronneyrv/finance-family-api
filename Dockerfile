# ==========================
# Stage 1 - Build
# ==========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

# ==========================
# Stage 2 - Runtime
# ==========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/finance.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]