-- ============================================
-- Script SQL para crear la base de datos CRM
-- Multinacional de Formación Profesional Superior
-- Base de datos: crudProject
-- ============================================

-- REINICIAR COMPLETAMENTE LA BD (OJO: BORRA TODO)
DROP DATABASE IF EXISTS crudProject;

CREATE DATABASE crudProject;

USE crudProject;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS facturas;

DROP TABLE IF EXISTS productos;

DROP TABLE IF EXISTS clientes;

DROP TABLE IF EXISTS comerciales;

DROP TABLE IF EXISTS secciones;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- CREAR TABLAS
-- ============================================

-- Tabla de secciones
CREATE TABLE secciones (
    seccion_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de comerciales
CREATE TABLE comerciales (
    comercial_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(150),
    email VARCHAR(150),
    telefono VARCHAR(30),
    rol VARCHAR(20) NOT NULL DEFAULT 'comercial',
    version INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de clientes
CREATE TABLE clientes (
    cliente_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(150),
    apellidos VARCHAR(150),
    edad INT,
    email VARCHAR(150),
    telefono VARCHAR(30),
    direccion VARCHAR(300),
    comercial_id INT,
    version INT DEFAULT 0,
    FOREIGN KEY (comercial_id) REFERENCES comerciales(comercial_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de productos
CREATE TABLE productos (
    producto_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio_base DECIMAL(12,2) NOT NULL,
    plazas_disponibles INT NOT NULL,
    seccion_id INT,
    FOREIGN KEY (seccion_id) REFERENCES secciones(seccion_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de facturas
CREATE TABLE facturas (
    factura_id VARCHAR(50) PRIMARY KEY,
    cliente_id INT NOT NULL,
    comercial_id INT,
    producto_id INT NOT NULL,
    fecha_emision DATETIME,
    estado VARCHAR(20) DEFAULT 'pendiente',
    subtotal DECIMAL(12,2),
    total_iva DECIMAL(12,2),
    total DECIMAL(12,2),
    version INT DEFAULT 0,
    FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id) ON DELETE CASCADE,
    FOREIGN KEY (comercial_id) REFERENCES comerciales(comercial_id) ON DELETE SET NULL,
    FOREIGN KEY (producto_id) REFERENCES productos(producto_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- INSERTAR DATOS DE EJEMPLO
-- ============================================

-- Insertar secciones (Áreas de formación profesional)
INSERT INTO secciones (nombre) VALUES
('Salud'),
('Emergencias'),
('Business'),
('Tech');

-- Insertar comerciales
INSERT INTO comerciales (username, password_hash, nombre, email, telefono, rol, version) VALUES
('admin', '123456', 'Dani Jaén', 'admin@crm.com', '600111222', 'pseudoadmin', 0),
('comercial1', '$2a$10$EjemploHashSeguro123456789', 'Juan Pérez', 'juan.perez@crm.com', '600222333', 'comercial', 0),
('comercial2', '$2a$10$EjemploHashSeguro123456789', 'María García', 'maria.garcia@crm.com', '600333444', 'comercial', 0),
('comercial3', '$2a$10$EjemploHashSeguro123456789', 'Carlos López', 'carlos.lopez@crm.com', '600444555', 'comercial', 0),
('comercial4', '$2a$10$EjemploHashSeguro123456789', 'Ana Martínez', 'ana.martinez@crm.com', '600555666', 'comercial', 0);

-- Insertar clientes (Estudiantes potenciales)
INSERT INTO clientes (username, password_hash, nombre, apellidos, edad, email, telefono, direccion, comercial_id, version) VALUES
('chumete', '123456', 'chumete', 'kotlin', 19, 'pedro.sanchez@email.com', '611111111', 'Calle Mayor 1, Madrid', 2, 0),
('cliente2', '$2a$10$EjemploHashSeguro123456789', 'Laura', 'Rodríguez', 20, 'laura.rodriguez@email.com', '622222222', 'Avenida Principal 15, Barcelona', 2, 0),
('cliente3', '$2a$10$EjemploHashSeguro123456789', 'Miguel', 'Fernández', 22, 'miguel.fernandez@email.com', '633333333', 'Plaza del Sol 8, Valencia', 3, 0),
('cliente4', '$2a$10$EjemploHashSeguro123456789', 'Isabel', 'Torres', 21, 'isabel.torres@email.com', '644444444', 'Calle Nueva 22, Sevilla', 3, 0),
('cliente5', '$2a$10$EjemploHashSeguro123456789', 'Roberto', 'Jiménez', 18, 'roberto.jimenez@email.com', '655555555', 'Paseo de la Alameda 45, Bilbao', 4, 0),
('cliente6', '$2a$10$EjemploHashSeguro123456789', 'Carmen', 'Ruiz', 23, 'carmen.ruiz@email.com', '666666666', 'Calle del Comercio 12, Málaga', 4, 0),
('cliente7', '$2a$10$EjemploHashSeguro123456789', 'David', 'Moreno', 19, 'david.moreno@email.com', '677777777', 'Avenida Libertad 30, Zaragoza', 5, 0),
('cliente8', '$2a$10$EjemploHashSeguro123456789', 'Sofía', 'Vázquez', 24, 'sofia.vazquez@email.com', '688888888', 'Plaza Central 7, Murcia', 5, 0),
('cliente9', '$2a$10$EjemploHashSeguro123456789', 'Javier', 'Herrera', 20, 'javier.herrera@email.com', '699999999', 'Calle Mayor 88, Palma', 2, 0),
('cliente10', '$2a$10$EjemploHashSeguro123456789', 'Elena', 'Díaz', 21, 'elena.diaz@email.com', '610101010', 'Avenida del Mar 33, Alicante', 3, 0);

-- Insertar productos (Cursos de Grados Superiores)
INSERT INTO productos (nombre, descripcion, precio_base, plazas_disponibles, seccion_id) VALUES
-- Cursos de Salud
('Grado Superior en Enfermería', 'Formación completa en cuidados de enfermería, anatomía, fisiología y atención al paciente. Incluye prácticas en centros sanitarios.', 4500.00, 60, 1),
('Grado Superior en Farmacia y Parafarmacia', 'Curso completo de gestión de oficinas de farmacia, dispensación de medicamentos y productos parafarmacéuticos.', 4200.00, 45, 1),
('Grado Superior en Técnico de Rayos', 'Formación especializada en técnicas de diagnóstico por imagen: radiología, TAC, resonancia magnética y ecografías.', 4800.00, 30, 1),
('Grado Superior en Dietética y Nutrición', 'Estudio de nutrición humana, dietoterapia, control alimentario y educación nutricional para la salud.', 3900.00, 50, 1),
('Grado Superior en Higiene Bucodental', 'Formación en prevención y tratamiento de patologías bucodentales, periodoncia y ortodoncia.', 4400.00, 40, 1),
('Grado Superior en Anatomía Patológica', 'Técnicas de análisis de muestras biológicas, citología, histología y laboratorio de diagnóstico.', 4600.00, 25, 1),
('Grado Superior en Laboratorio Clínico', 'Análisis clínicos, microbiología, bioquímica y técnicas de laboratorio para diagnóstico médico.', 4500.00, 35, 1),

-- Cursos de Emergencias
('Grado Superior en Emergencias Sanitarias', 'Formación en atención prehospitalaria, técnicas de soporte vital y transporte sanitario. Título oficial de Técnico en Emergencias Sanitarias.', 4700.00, 55, 2),
('Grado Superior en Protección Civil', 'Gestión de emergencias, planes de evacuación, coordinación de recursos y protocolos de seguridad ciudadana.', 4000.00, 40, 2),
('Grado Superior en Coordinación de Emergencias', 'Organización y dirección de equipos de respuesta ante emergencias, catástrofes y situaciones de crisis.', 4600.00, 30, 2),
('Grado Superior en Prevención de Riesgos Profesionales', 'Identificación y evaluación de riesgos laborales, planes de prevención y seguridad en el trabajo.', 3800.00, 50, 2),

-- Cursos de Business
('Grado Superior en Administración y Finanzas', 'Contabilidad, fiscalidad, gestión administrativa y análisis financiero empresarial.', 3500.00, 80, 3),
('Grado Superior en Comercio Internacional', 'Gestión de operaciones de comercio exterior, aduanas, logística internacional y marketing global.', 4000.00, 60, 3),
('Grado Superior en Marketing y Publicidad', 'Estrategias de marketing digital, publicidad, investigación de mercados y gestión de marca.', 3700.00, 70, 3),
('Grado Superior en Gestión de Ventas', 'Técnicas comerciales, atención al cliente, gestión de equipos comerciales y estrategias de venta.', 3600.00, 65, 3),
('Grado Superior en Asistencia a la Dirección', 'Organización ejecutiva, gestión documental, protocolo empresarial y apoyo directivo multilingüe.', 3900.00, 55, 3),
('Grado Superior en Transporte y Logística', 'Gestión de almacenes, distribución, transporte internacional y cadena de suministro.', 3800.00, 50, 3),

-- Cursos de Tech
('Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM)', 'Programación multiplataforma, desarrollo móvil Android/iOS, bases de datos y aplicaciones web. Incluye prácticas en empresas tecnológicas.', 4200.00, 90, 4),
('Grado Superior en Administración de Sistemas Informáticos en Red (ASIR)', 'Administración de servidores, redes, sistemas operativos, virtualización y ciberseguridad. Preparación para certificaciones profesionales.', 4400.00, 75, 4),
('Grado Superior en Desarrollo de Aplicaciones Web (DAW)', 'Desarrollo full-stack, lenguajes de programación web, frameworks modernos y arquitecturas de software.', 4200.00, 85, 4),
('Grado Superior en Ciberseguridad', 'Seguridad informática, hacking ético, análisis forense, protección de datos y sistemas de seguridad.', 4600.00, 50, 4),
('Grado Superior en Inteligencia Artificial y Big Data', 'Machine Learning, análisis de datos, procesamiento de grandes volúmenes y algoritmos de IA.', 4800.00, 40, 4),
('Grado Superior en Animación 3D y Videojuegos', 'Modelado 3D, animación, desarrollo de videojuegos, realidad virtual y efectos visuales.', 4500.00, 45, 4);

-- Insertar facturas (Matrículas en cursos)
INSERT INTO facturas (factura_id, cliente_id, comercial_id, producto_id, fecha_emision, estado, subtotal, total_iva, total, version) VALUES
('FAC-2024-001', 1, 2, 18, '2024-01-15 10:30:00', 'pagada', 4200.00, 882.00, 5082.00, 0),
('FAC-2024-002', 2, 2, 17, '2024-01-16 14:20:00', 'pagada', 4200.00, 882.00, 5082.00, 0),
('FAC-2024-003', 3, 3, 3, '2024-01-17 09:15:00', 'pendiente', 4800.00, 1008.00, 5808.00, 0),
('FAC-2024-004', 4, 3, 12, '2024-01-18 16:45:00', 'pagada', 3500.00, 735.00, 4235.00, 0),
('FAC-2024-005', 5, 4, 20, '2024-01-19 11:00:00', 'pagada', 4600.00, 966.00, 5566.00, 0),
('FAC-2024-006', 6, 4, 8, '2024-01-20 13:30:00', 'pagada', 4700.00, 987.00, 5687.00, 0),
('FAC-2024-007', 7, 5, 19, '2024-01-21 10:00:00', 'pendiente', 4400.00, 924.00, 5324.00, 0),
('FAC-2024-008', 8, 5, 13, '2024-01-22 15:20:00', 'pagada', 4000.00, 840.00, 4840.00, 0),
('FAC-2024-009', 9, 2, 1, '2024-01-23 08:45:00', 'pagada', 4500.00, 945.00, 5445.00, 0),
('FAC-2024-010', 10, 3, 22, '2024-01-24 12:10:00', 'pagada', 4800.00, 1008.00, 5808.00, 0),
('FAC-2024-011', 1, 2, 14, '2024-02-01 10:30:00', 'pagada', 3700.00, 777.00, 4477.00, 0),
('FAC-2024-012', 2, 2, 11, '2024-02-02 14:20:00', 'pendiente', 4000.00, 840.00, 4840.00, 0),
('FAC-2024-013', 3, 3, 7, '2024-02-03 09:15:00', 'pagada', 4500.00, 945.00, 5445.00, 0),
('FAC-2024-014', 4, 3, 15, '2024-02-04 16:45:00', 'pagada', 3600.00, 756.00, 4356.00, 0),
('FAC-2024-015', 5, 4, 21, '2024-02-05 11:00:00', 'pagada', 4500.00, 945.00, 5445.00, 0);

-- ============================================
-- VERIFICAR DATOS INSERTADOS
-- ============================================
SELECT 'Secciones insertadas:' AS Info, COUNT(*) AS Total FROM secciones
UNION ALL
SELECT 'Comerciales insertados:', COUNT(*) FROM comerciales
UNION ALL
SELECT 'Clientes insertados:', COUNT(*) FROM clientes
UNION ALL
SELECT 'Productos insertados:', COUNT(*) FROM productos
UNION ALL
SELECT 'Facturas insertadas:', COUNT(*) FROM facturas;
