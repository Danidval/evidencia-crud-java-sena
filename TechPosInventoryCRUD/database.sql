-- =============================================
-- TECHPOS INVENTORY DATABASE
-- Sistema de Gestión de Inventario
-- CRUD Java + MySQL + JOptionPane
-- =============================================
-- Autor: Danid_Vallejos
-- Fecha: Abril 2026
-- Base de Datos: db_techpos_v1
-- =============================================

-- 1. CREAR BASE DE DATOS
-- =============================================
DROP DATABASE IF EXISTS db_techpos_v1;
CREATE DATABASE db_techpos_v1;
USE db_techpos_v1;

-- 2. CREAR TABLA DE CATEGORIAS
-- =============================================
CREATE TABLE CategoriaProducto (
    idCategoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. CREAR TABLA DE PRODUCTOS (PRINCIPAL DEL CRUD)
-- =============================================
CREATE TABLE ProductoInventario (
    idProducto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    stock_minimo INT DEFAULT 5,
    ubicacion VARCHAR(50) DEFAULT 'No asignada',
    idCategoria INT,
    FOREIGN KEY (idCategoria) REFERENCES CategoriaProducto(idCategoria)
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);

-- 4. INSERTAR CATEGORIAS INICIALES
-- =============================================
INSERT INTO CategoriaProducto (nombre, descripcion) VALUES 
('Repuestos Pantallas', 'Pantallas LCD, OLED y tactiles para dispositivos moviles y laptops'),
('Baterias', 'Baterias internas y externas para moviles y laptops'),
('Cargadores', 'Cargadores rapidos y cables USB'),
('Accesorios', 'Fundas, protectores de pantalla y audifonos');

-- 5. INSERTAR PRODUCTOS DE PRUEBA
-- =============================================
INSERT INTO ProductoInventario (nombre, descripcion, precio, stock, stock_minimo, ubicacion, idCategoria) VALUES 
('Pantalla iPhone 12', 'OLED Calidad Original con herramienta incluida', 350.00, 10, 5, 'Estante A-101', 1),
('Pantalla Samsung S21', 'Dynamic AMOLED 2X 120Hz', 320.00, 8, 5, 'Estante A-102', 1),
('Bateria MacBook Air A1466', 'Compatible original 54Wh', 180.00, 5, 3, 'Vitrina Principal', 2),
('Bateria iPhone 11', '3110mAh calidad premium', 85.00, 15, 5, 'Vitrina Secundaria', 2),
('Cargador Rapido 20W', 'USB-C Power Delivery incluye cable', 45.00, 20, 10, 'Gaveta C-01', 3),
('Funda iPhone 12', 'Silicone color negro', 25.00, 30, 10, 'Estante D-01', 4);

-- 6. ACTUALIZAR UBICACIONES PENDIENTES
-- =============================================
UPDATE ProductoInventario SET ubicacion = 'No asignada' WHERE ubicacion IS NULL;

-- 7. CONSULTAS DE VERIFICACION
-- =============================================
-- Ver todas las categorias
SELECT * FROM CategoriaProducto;

-- Ver todos los productos con nombre de categoria
SELECT 
    p.idProducto,
    p.nombre AS producto,
    p.precio,
    p.stock,
    p.stock_minimo,
    p.ubicacion,
    c.nombre AS categoria,
    CASE 
        WHEN p.stock <= p.stock_minimo THEN 'Stock bajo'
        ELSE 'Stock normal'
    END AS estado_stock
FROM ProductoInventario p
LEFT JOIN CategoriaProducto c ON p.idCategoria = c.idCategoria
ORDER BY p.idProducto;

-- 8. VISTAS UTILES PARA EL CRUD
-- =============================================
-- Vista de productos con stock bajo
CREATE VIEW vw_stock_bajo AS
SELECT 
    p.idProducto,
    p.nombre,
    p.stock,
    p.stock_minimo,
    c.nombre AS categoria
FROM ProductoInventario p
LEFT JOIN CategoriaProducto c ON p.idCategoria = c.idCategoria
WHERE p.stock <= p.stock_minimo;

-- Vista de resumen de inventario
CREATE VIEW vw_resumen_inventario AS
SELECT 
    COUNT(*) AS total_productos,
    SUM(stock) AS unidades_totales,
    SUM(precio * stock) AS valor_inventario,
    COUNT(CASE WHEN stock <= stock_minimo THEN 1 END) AS productos_stock_bajo
FROM ProductoInventario;

-- 9. PROCEDIMIENTOS ALMACENADOS
-- =============================================
DELIMITER //

-- Procedimiento para actualizar stock
CREATE PROCEDURE sp_actualizar_stock(
    IN p_idProducto INT,
    IN p_nuevo_stock INT
)
BEGIN
    UPDATE ProductoInventario 
    SET stock = p_nuevo_stock 
    WHERE idProducto = p_idProducto;
    
    SELECT CONCAT('Stock actualizado a ', p_nuevo_stock) AS mensaje;
END //

-- Procedimiento para obtener productos por categoria
CREATE PROCEDURE sp_productos_por_categoria(
    IN p_idCategoria INT
)
BEGIN
    SELECT * FROM ProductoInventario 
    WHERE idCategoria = p_idCategoria 
    ORDER BY nombre;
END //

DELIMITER ;

-- 10. MOSTRAR RESULTADOS FINALES
-- =============================================
SELECT '=== CATEGORIAS REGISTRADAS ===' AS '';
SELECT * FROM CategoriaProducto;

SELECT '=== PRODUCTOS REGISTRADOS ===' AS '';
SELECT * FROM ProductoInventario;

SELECT '=== PRODUCTOS CON STOCK BAJO ===' AS '';
SELECT * FROM vw_stock_bajo;

SELECT '=== RESUMEN DEL INVENTARIO ===' AS '';
SELECT * FROM vw_resumen_inventario;

-- =============================================
-- FIN DEL SCRIPT
-- =============================================
