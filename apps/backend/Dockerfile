# Dockerfile para Backend - Clínica Veterinaria
# Multi-stage build para optimizar el tamaño de la imagen

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar todo el contexto para detectar la estructura del proyecto
COPY . /tmp/build

# Debug: mostrar estructura de directorios
RUN echo "=== Estructura de directorios ===" && \
    ls -la /tmp/build/ && \
    echo "=== Buscando apps/backend ===" && \
    ls -la /tmp/build/apps/ 2>/dev/null || echo "No existe apps/" && \
    echo "=== Buscando apps/backend/src ===" && \
    ls -la /tmp/build/apps/backend/ 2>/dev/null || echo "No existe apps/backend/"

# Detectar y copiar pom.xml (funciona desde raíz o apps/backend)
RUN if [ -f /tmp/build/apps/backend/pom.xml ]; then \
        echo "Copiando pom.xml desde apps/backend/" && \
        cp /tmp/build/apps/backend/pom.xml /app/pom.xml; \
    elif [ -f /tmp/build/pom.xml ]; then \
        echo "Copiando pom.xml desde raíz" && \
        cp /tmp/build/pom.xml /app/pom.xml; \
    else \
        echo "Error: pom.xml no encontrado" && \
        echo "Buscando en:" && \
        find /tmp/build -name "pom.xml" -type f 2>/dev/null || echo "No se encontró pom.xml" && \
        exit 1; \
    fi

# Descargar dependencias (se cachean si no cambia pom.xml)
RUN mvn dependency:go-offline -B || true

# Detectar y copiar código fuente (funciona desde raíz o apps/backend)
RUN if [ -d /tmp/build/apps/backend/src ]; then \
        echo "Copiando src desde apps/backend/" && \
        cp -r /tmp/build/apps/backend/src /app/src && \
        echo "Verificando src copiado:" && \
        ls -la /app/src/; \
    elif [ -d /tmp/build/src ]; then \
        echo "Copiando src desde raíz" && \
        cp -r /tmp/build/src /app/src && \
        echo "Verificando src copiado:" && \
        ls -la /app/src/; \
    else \
        echo "Error: directorio src no encontrado" && \
        echo "Buscando directorios src:" && \
        find /tmp/build -type d -name "src" 2>/dev/null || echo "No se encontró ningún directorio src" && \
        exit 1; \
    fi

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
