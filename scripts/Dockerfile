FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace
COPY settings.gradle.kts build.gradle.kts gradlew gradlew.bat gradle/ ./
COPY server/settings.gradle.kts server/build.gradle.kts ./server/
COPY shared/build.gradle.kts ./shared/
COPY server/src server/src
RUN ./gradlew :server:installDist --no-daemon

FROM mcr.microsoft.com/windows/servercore:ltsc2022
WORKDIR /app
COPY --from=builder /workspace/server/build/install/server /app/server
COPY scripts/start-server.bat /app/scripts/start-server.bat
RUN powershell -Command "Set-ExecutionPolicy Bypass -Scope Process -Force"
EXPOSE 8080 8443
ENTRYPOINT ["cmd", "/c", "scripts\start-server.bat"]
