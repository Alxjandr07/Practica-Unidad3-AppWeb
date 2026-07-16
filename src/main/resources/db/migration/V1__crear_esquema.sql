-- V1: Esquema base del sistema (usuarios + vehiculos de la cooperativa de transporte)
-- Practica Experimental Unidad III - Aplicaciones Web

CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'OPERADOR',
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rol CHECK (rol IN ('ADMIN', 'OPERADOR', 'CONDUCTOR'))
);

CREATE TABLE vehiculos (
    id                  BIGSERIAL PRIMARY KEY,
    placa               VARCHAR(10)  NOT NULL UNIQUE,
    marca               VARCHAR(50)  NOT NULL,
    modelo              VARCHAR(50)  NOT NULL,
    anio_fabricacion    INTEGER      NOT NULL,
    capacidad_pasajeros INTEGER      NOT NULL,
    tipo_servicio       VARCHAR(20)  NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    conductor_asignado  VARCHAR(150),
    ruta_asignada       VARCHAR(150),
    fecha_registro      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tipo_servicio CHECK (tipo_servicio IN ('URBANO', 'INTERPROVINCIAL', 'ESCOLAR', 'CARGA_LIVIANA')),
    CONSTRAINT chk_estado CHECK (estado IN ('ACTIVO', 'MANTENIMIENTO', 'INACTIVO', 'REVISION_TECNICA'))
);

CREATE INDEX idx_vehiculos_estado ON vehiculos(estado);
CREATE INDEX idx_vehiculos_tipo_servicio ON vehiculos(tipo_servicio);
CREATE INDEX idx_vehiculos_placa ON vehiculos(placa);
