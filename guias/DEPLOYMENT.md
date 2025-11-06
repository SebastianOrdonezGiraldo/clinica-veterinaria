# 🚀 Guía de Despliegue - Clínica Veterinaria

Esta guía cubre diferentes opciones para desplegar el sistema en producción.

## 📋 Tabla de Contenidos

- [Preparación](#preparación)
- [Despliegue en Servidor Local](#despliegue-en-servidor-local)
- [Despliegue con Docker](#despliegue-con-docker)
- [Despliegue en la Nube](#despliegue-en-la-nube)
- [Configuración de Producción](#configuración-de-producción)
- [Monitoreo y Mantenimiento](#monitoreo-y-mantenimiento)

## 🎯 Preparación

### 1. Compilar el Proyecto

```bash
cd backend
mvn clean package -DskipTests
```

El JAR se generará en: `backend/target/veterinaria-1.0.0.jar`

### 2. Configurar Variables de Entorno

Crea un archivo `.env` con las credenciales de producción:

```bash
# Base de datos
DB_HOST=tu-servidor-db.com
DB_PORT=5432
DB_NAME=vetclinic_prod
DB_USERNAME=veterinaria_user
DB_PASSWORD=password_segura_aqui

# JWT
JWT_SECRET=clave_secreta_muy_larga_y_segura_de_al_menos_256_bits
JWT_EXPIRATION=36000000

# Aplicación
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

## 🖥️ Despliegue en Servidor Local

### Opción 1: Ejecutar JAR Directamente

```bash
# Exportar variables
export $(cat .env | xargs)

# Ejecutar
java -jar backend/target/veterinaria-1.0.0.jar
```

### Opción 2: Crear Servicio Systemd (Linux)

1. Crear archivo de servicio:

```bash
sudo nano /etc/systemd/system/clinica-veterinaria.service
```

2. Agregar contenido:

```ini
[Unit]
Description=Clinica Veterinaria API
After=syslog.target network.target postgresql.service

[Service]
User=veterinaria
Group=veterinaria
WorkingDirectory=/opt/clinica-veterinaria
ExecStart=/usr/bin/java -jar /opt/clinica-veterinaria/veterinaria-1.0.0.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

Environment="DB_HOST=localhost"
Environment="DB_PORT=5432"
Environment="DB_NAME=vetclinic_prod"
Environment="DB_USERNAME=veterinaria"
Environment="DB_PASSWORD=tu_password"
Environment="JWT_SECRET=tu_secret"
Environment="JWT_EXPIRATION=36000000"

[Install]
WantedBy=multi-user.target
```

3. Habilitar y ejecutar:

```bash
sudo systemctl daemon-reload
sudo systemctl enable clinica-veterinaria
sudo systemctl start clinica-veterinaria
sudo systemctl status clinica-veterinaria
```

### Opción 3: Usar PM2 (Node.js Process Manager)

```bash
# Instalar PM2
npm install -g pm2

# Crear script de inicio
cat > start.sh << 'EOF'
#!/bin/bash
export $(cat .env | xargs)
java -jar backend/target/veterinaria-1.0.0.jar
EOF

chmod +x start.sh

# Ejecutar con PM2
pm2 start start.sh --name clinica-veterinaria
pm2 save
pm2 startup
```

## 🐳 Despliegue con Docker

### 1. Crear Dockerfile

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR
COPY target/veterinaria-1.0.0.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Crear docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: veterinaria-db
    environment:
      POSTGRES_DB: vetclinic_prod
      POSTGRES_USER: veterinaria
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - veterinaria-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U veterinaria"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: veterinaria-api
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: vetclinic_prod
      DB_USERNAME: veterinaria
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: 36000000
    ports:
      - "8080:8080"
    networks:
      - veterinaria-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: veterinaria-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - backend
    networks:
      - veterinaria-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  veterinaria-network:
    driver: bridge
```

### 3. Configurar Nginx

```nginx
# nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server backend:8080;
    }

    server {
        listen 80;
        server_name tu-dominio.com;
        
        # Redirigir a HTTPS
        return 301 https://$host$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name tu-dominio.com;

        # Certificados SSL
        ssl_certificate /etc/nginx/ssl/certificate.crt;
        ssl_certificate_key /etc/nginx/ssl/private.key;

        # Configuración SSL
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;
        ssl_prefer_server_ciphers on;

        # Proxy a backend
        location /api/ {
            proxy_pass http://backend/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Documentación Swagger
        location /swagger-ui/ {
            proxy_pass http://backend/swagger-ui/;
            proxy_set_header Host $host;
        }
    }
}
```

### 4. Desplegar

```bash
# Compilar proyecto
mvn clean package -DskipTests

# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Verificar estado
docker-compose ps
```

## ☁️ Despliegue en la Nube

### AWS (Amazon Web Services)

#### Opción 1: EC2 + RDS

1. **Crear instancia RDS (PostgreSQL)**:
   - Tipo: db.t3.micro (capa gratuita)
   - Almacenamiento: 20 GB
   - Habilitar backups automáticos

2. **Crear instancia EC2**:
   - AMI: Amazon Linux 2
   - Tipo: t2.micro (capa gratuita)
   - Security Group: Permitir puerto 8080

3. **Desplegar**:

```bash
# Conectar a EC2
ssh -i clave.pem ec2-user@tu-ip-publica

# Instalar Java
sudo yum install java-17-amazon-corretto -y

# Subir JAR
scp -i clave.pem target/veterinaria-1.0.0.jar ec2-user@tu-ip:/home/ec2-user/

# Configurar y ejecutar
export DB_HOST=tu-rds-endpoint.rds.amazonaws.com
export DB_USERNAME=admin
export DB_PASSWORD=tu_password
# ... más variables

nohup java -jar veterinaria-1.0.0.jar > app.log 2>&1 &
```

#### Opción 2: Elastic Beanstalk

```bash
# Instalar EB CLI
pip install awsebcli

# Inicializar
eb init -p java-17 clinica-veterinaria

# Crear ambiente
eb create prod-env

# Configurar variables
eb setenv DB_HOST=xxx DB_USERNAME=xxx ...

# Desplegar
eb deploy

# Abrir en navegador
eb open
```

#### Opción 3: ECS (Elastic Container Service)

```bash
# Construir imagen
docker build -t veterinaria:latest backend/

# Crear repositorio ECR
aws ecr create-repository --repository-name clinica-veterinaria

# Autenticar Docker con ECR
aws ecr get-login-password | docker login --username AWS --password-stdin xxx.dkr.ecr.region.amazonaws.com

# Tag y push
docker tag veterinaria:latest xxx.dkr.ecr.region.amazonaws.com/clinica-veterinaria:latest
docker push xxx.dkr.ecr.region.amazonaws.com/clinica-veterinaria:latest

# Crear servicio ECS con Fargate
```

### Heroku

```bash
# Instalar Heroku CLI
heroku login

# Crear app
heroku create clinica-veterinaria-api

# Agregar PostgreSQL
heroku addons:create heroku-postgresql:mini

# Configurar variables
heroku config:set JWT_SECRET=tu_secret
heroku config:set SPRING_PROFILES_ACTIVE=prod

# Configurar Java buildpack
echo "java.runtime.version=17" > system.properties
git add system.properties

# Desplegar
git push heroku main

# Ver logs
heroku logs --tail
```

### DigitalOcean

#### App Platform

1. Conectar repositorio GitHub
2. Seleccionar rama `main`
3. Configurar:
   - Build Command: `mvn clean package -DskipTests`
   - Run Command: `java -jar target/veterinaria-1.0.0.jar`
4. Agregar PostgreSQL managed database
5. Configurar variables de entorno
6. Deploy

### Google Cloud Platform

#### Cloud Run

```bash
# Autenticar
gcloud auth login

# Configurar proyecto
gcloud config set project tu-proyecto-id

# Construir y subir imagen
gcloud builds submit --tag gcr.io/tu-proyecto/clinica-veterinaria

# Desplegar
gcloud run deploy clinica-veterinaria \
  --image gcr.io/tu-proyecto/clinica-veterinaria \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="DB_HOST=xxx,DB_USERNAME=xxx,..."
```

## ⚙️ Configuración de Producción

### 1. Optimizaciones de Base de Datos

```properties
# application-prod.properties

# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### 2. Logging

```properties
# Logs a archivo
logging.file.name=/var/log/clinica-veterinaria/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Niveles
logging.level.root=WARN
logging.level.com.clinica.veterinaria=INFO
logging.level.org.springframework.web=INFO
```

### 3. Seguridad Adicional

```properties
# HTTPS only
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12

# Headers de seguridad
server.http2.enabled=true
```

## 📊 Monitoreo y Mantenimiento

### Spring Boot Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# Habilitar endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

### Health Checks

```bash
# Health check
curl http://tu-servidor:8080/actuator/health

# Métricas
curl http://tu-servidor:8080/actuator/metrics
```

### Backups de Base de Datos

```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
DB_NAME="vetclinic_prod"

# Crear backup
pg_dump -h localhost -U veterinaria $DB_NAME | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# Mantener solo últimos 7 días
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete
```

Agregar a crontab:
```bash
# Backup diario a las 2 AM
0 2 * * * /path/to/backup.sh
```

### Actualización de la Aplicación

```bash
# Zero-downtime deployment con PM2
pm2 reload clinica-veterinaria

# Con systemd
sudo systemctl restart clinica-veterinaria

# Con Docker
docker-compose pull
docker-compose up -d --no-deps --build backend
```

## 🔒 Checklist de Seguridad

- [ ] HTTPS habilitado
- [ ] JWT secret fuerte (256+ bits)
- [ ] Variables de entorno securizadas
- [ ] Firewall configurado
- [ ] Base de datos con acceso restringido
- [ ] Backups automáticos habilitados
- [ ] Logs protegidos
- [ ] Rate limiting configurado
- [ ] Headers de seguridad configurados
- [ ] Dependencias actualizadas

## 📈 Escalamiento

### Horizontal (Múltiples Instancias)

1. **Load Balancer**: Nginx o AWS ALB
2. **Session Management**: JWT es stateless (✓)
3. **Database**: Replica read slaves
4. **Cache**: Redis para sesiones compartidas

### Vertical (Más Recursos)

```bash
# Ajustar memoria JVM
java -Xmx2g -Xms1g -jar veterinaria-1.0.0.jar
```

---

**Última actualización**: Noviembre 2025

