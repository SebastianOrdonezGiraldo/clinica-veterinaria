-- Script para crear la base de datos en Railway
-- Ejecutar en el servicio PostgreSQL de Railway

-- Crear la base de datos si no existe
SELECT 'CREATE DATABASE vetclinic_dev'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vetclinic_dev')\gexec

-- Conectar a la nueva base de datos
\c vetclinic_dev

-- Comentario sobre la base de datos
COMMENT ON DATABASE vetclinic_dev IS 'Base de datos principal - Clínica Veterinaria';

