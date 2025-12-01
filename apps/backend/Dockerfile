# Dockerfile para Backend - Clínica Veterinaria
# Multi-stage build para optimizar el tamaño de la imagen

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar pom.xml primero (para cachear dependencias)
COPY pom.xml /app/pom.xml

# Descargar dependencias (se cachean si no cambia pom.xml)
RUN mvn dependency:go-offline -B || true

# Copiar código fuente
COPY src /app/src

# Limpiar contexto temporal para reducir tamaño de imagen
RUN rm -rf /tmp/build

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio de logs
RUN mkdir -p /app/logs && chown spring:spring /app/logs

# Copiar JAR desde stage de build
COPY --from=build /app/target/veterinaria-1.0.0.jar app.jar

# Cambiar ownership al usuario spring
RUN chown spring:spring app.jar

USER spring:spring

# Establecer directorio de trabajo
WORKDIR /app

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
