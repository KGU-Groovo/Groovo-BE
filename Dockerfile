FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon
RUN cp "$(find build/libs -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" app.jar

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S groovo && adduser -S groovo -G groovo

COPY --from=builder /workspace/app.jar app.jar

USER groovo
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
