-- Script para agregar la columna version a las tablas que usan optimistic locking
-- Ejecuta este script en tu base de datos MySQL

USE crudProject;

-- Agregar columna version a la tabla clientes
ALTER TABLE clientes 
ADD COLUMN version INT DEFAULT 0 NOT NULL;

-- Agregar columna version a la tabla comerciales
ALTER TABLE comerciales 
ADD COLUMN version INT DEFAULT 0 NOT NULL;

-- Agregar columna version a la tabla facturas
ALTER TABLE facturas 
ADD COLUMN version INT DEFAULT 0 NOT NULL;

-- Verificar que las columnas se agregaron correctamente
SELECT 'clientes' AS tabla, COUNT(*) AS registros FROM clientes;
SELECT 'comerciales' AS tabla, COUNT(*) AS registros FROM comerciales;
SELECT 'facturas' AS tabla, COUNT(*) AS registros FROM facturas;

