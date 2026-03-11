CREATE TABLE IF NOT EXISTS historial_alertas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alerta_id BIGINT NOT NULL,
    estacion_codigo VARCHAR(100) NOT NULL,
    sensor_tipo VARCHAR(120) NOT NULL,
    operador VARCHAR(5) NOT NULL,
    umbral DECIMAL(10,2) NOT NULL,
    valor_detectado DECIMAL(10,2) NULL,
    fecha_activacion DATETIME NOT NULL,
    CONSTRAINT fk_historial_alertas_alerta
        FOREIGN KEY (alerta_id) REFERENCES alertas(id_alerta)
        ON DELETE CASCADE
);
