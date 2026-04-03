package org.example;

import javax.swing.JOptionPane;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/db_techpos_v1?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
        String user = "root";
        String password = "3333";

        boolean salir = false;

        while (!salir) {
            Connection conn = null;
            try {
                // Menú Principal
                String menu = "--- GESTIÓN DE INVENTARIO TECHPOS ---\n" +
                        "1. Registrar nuevo producto\n" +
                        "2. Ver lista de inventario\n" +
                        "3. Actualizar precio o stock\n" +
                        "4. Eliminar un producto\n" +
                        "5. Salir\n\n" +
                        "Seleccione una opción:";

                String opcion = JOptionPane.showInputDialog(null, menu, "TechPos v1.0", JOptionPane.PLAIN_MESSAGE);

                if (opcion == null || opcion.equals("5")) {
                    salir = true;
                    break;
                }

                // Establecer conexión
                conn = DriverManager.getConnection(url, user, password);

                switch (opcion) {
                    case "1": // CREATE
                        String nombre = JOptionPane.showInputDialog("Nombre del producto:");
                        if (nombre == null || nombre.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "El nombre es obligatorio.");
                            break;
                        }

                        String desc = JOptionPane.showInputDialog("Descripción:");
                        if (desc == null) desc = "";

                        String precioStr = JOptionPane.showInputDialog("Precio (ej: 350.00):");
                        if (precioStr == null) break;
                        double precio = Double.parseDouble(precioStr);

                        String stockStr = JOptionPane.showInputDialog("Cantidad en stock:");
                        if (stockStr == null) break;
                        int stock = Integer.parseInt(stockStr);

                        String ubi = JOptionPane.showInputDialog("Ubicación (ej: Estante A1):");
                        if (ubi == null) ubi = "No asignada";

                        String idCatStr = JOptionPane.showInputDialog("ID Categoría (1: Repuestos Pantallas, 2: Baterías):");
                        if (idCatStr == null) break;
                        int idCat = Integer.parseInt(idCatStr);

                        // Usando PreparedStatement para evitar inyección SQL
                        String sqlInsert = "INSERT INTO ProductoInventario (nombre, descripcion, precio, stock, ubicacion, idCategoria) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                            pstmt.setString(1, nombre);
                            pstmt.setString(2, desc);
                            pstmt.setDouble(3, precio);
                            pstmt.setInt(4, stock);
                            pstmt.setString(5, ubi);
                            pstmt.setInt(6, idCat);
                            pstmt.executeUpdate();
                        }
                        JOptionPane.showMessageDialog(null, "Producto registrado con éxito.");
                        break;

                    case "2": // READ
                        String query = "SELECT p.idProducto, p.nombre, p.precio, p.stock, p.ubicacion, c.nombre as cat_nombre " +
                                "FROM ProductoInventario p " +
                                "LEFT JOIN CategoriaProducto c ON p.idCategoria = c.idCategoria";

                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery(query)) {

                            StringBuilder lista = new StringBuilder("--- INVENTARIO ACTUAL ---\n\n");
                            boolean hayDatos = false;

                            while (rs.next()) {
                                hayDatos = true;
                                lista.append("ID: ").append(rs.getInt("idProducto"))
                                        .append(" | ").append(rs.getString("nombre"))
                                        .append("\nPrecio: $").append(rs.getDouble("precio"))
                                        .append(" | Stock: ").append(rs.getInt("stock"))
                                        .append("\nUbicación: ").append(rs.getString("ubicacion") != null ? rs.getString("ubicacion") : "No asignada")
                                        .append(" | Cat: ").append(rs.getString("cat_nombre") != null ? rs.getString("cat_nombre") : "Sin categoría")
                                        .append("\n-----------------------------------\n");
                            }

                            if (!hayDatos) {
                                JOptionPane.showMessageDialog(null, "El inventario está vacío.");
                            } else {
                                JOptionPane.showMessageDialog(null, lista.toString());
                            }
                        }
                        break;

                    case "3": // UPDATE
                        String idModStr = JOptionPane.showInputDialog("Ingrese el ID del producto a modificar:");
                        if (idModStr == null) break;
                        int idMod = Integer.parseInt(idModStr);

                        String nPrecioStr = JOptionPane.showInputDialog("Nuevo Precio:");
                        if (nPrecioStr == null) break;
                        double nPrecio = Double.parseDouble(nPrecioStr);

                        String nStockStr = JOptionPane.showInputDialog("Nuevo Stock:");
                        if (nStockStr == null) break;
                        int nStock = Integer.parseInt(nStockStr);

                        String sqlUpdate = "UPDATE ProductoInventario SET precio = ?, stock = ? WHERE idProducto = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                            pstmt.setDouble(1, nPrecio);
                            pstmt.setInt(2, nStock);
                            pstmt.setInt(3, idMod);
                            int filas = pstmt.executeUpdate();

                            if (filas > 0) {
                                JOptionPane.showMessageDialog(null, "Datos actualizados correctamente.");
                            } else {
                                JOptionPane.showMessageDialog(null, "No se encontró ningún producto con ese ID.");
                            }
                        }
                        break;

                    case "4": // DELETE
                        String idDelStr = JOptionPane.showInputDialog("Ingrese el ID del producto a eliminar:");
                        if (idDelStr == null) break;
                        int idDel = Integer.parseInt(idDelStr);

                        int confirmar = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar el producto ID: " + idDel + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

                        if (confirmar == JOptionPane.YES_OPTION) {
                            String sqlDel = "DELETE FROM ProductoInventario WHERE idProducto = ?";
                            try (PreparedStatement pstmt = conn.prepareStatement(sqlDel)) {
                                pstmt.setInt(1, idDel);
                                int filasDel = pstmt.executeUpdate();

                                if (filasDel > 0) {
                                    JOptionPane.showMessageDialog(null, "Producto eliminado.");
                                } else {
                                    JOptionPane.showMessageDialog(null, "El ID no existe.");
                                }
                            }
                        }
                        break;

                    default:
                        JOptionPane.showMessageDialog(null, "Opción no válida.");
                        break;
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Error: Ingrese solo números en Precio, Stock e IDs.\n" + e.getMessage(), "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error de Base de Datos: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error de Sistema: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Cerrar conexión de forma segura
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.err.println("Error al cerrar la conexión: " + e.getMessage());
                    }
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Gracias por usar TechPos. ¡Hasta pronto!");
    }
}