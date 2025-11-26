# Dockerfile para Backend - Clínica Veterinaria
# Multi-stage build para optimizar el tamaño de la imagen

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Descargar dependencias (se cachean si no cambia pom.xml)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR desde stage de build
COPY --from=build /app/target/veterinaria-1.0.0.jar app.jar

# Exponer puerto (Railway/Render lo asignará automáticamente)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

