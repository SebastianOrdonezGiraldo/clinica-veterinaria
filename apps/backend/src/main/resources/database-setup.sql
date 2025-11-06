-- =================================================================
-- SCRIPT DE CONFIGURACIÓN DE BASE DE DATOS POSTGRESQL
-- Clínica Veterinaria
-- =================================================================

-- 1. Conectarse a PostgreSQL como superusuario y ejecutar:
-- psql -U postgres

-- 2. Crear la base de datos principal
DROP DATABASE IF EXISTS vetclinic;
CREATE DATABASE vetclinic
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Spain.1252'
    LC_CTYPE = 'Spanish_Spain.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- 3. Crear la base de datos de desarrollo (opcional)
DROP DATABASE IF EXISTS vetclinic_dev;
CREATE DATABASE vetclinic_dev
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Spain.1252'
    LC_CTYPE = 'Spanish_Spain.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- 4. Comentario sobre las bases de datos
COMMENT ON DATABASE vetclinic IS 'Base de datos principal - Clínica Veterinaria';
COMMENT ON DATABASE vetclinic_dev IS 'Base de datos de desarrollo - Clínica Veterinaria';

-- =================================================================
-- NOTAS:
-- - Las tablas se crearán automáticamente por Hibernate
-- - Los datos iniciales se cargarán desde data.sql
-- - Usuario por defecto: postgres
-- - Contraseña por defecto: postgres (cambiar en producción)
-- =================================================================

