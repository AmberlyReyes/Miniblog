package app.util;

import app.servicios.DatabaseCock;

import java.sql.Connection;
import java.sql.SQLException;

public class TestCock {
    public static void main(String[] args) {
        try (Connection conn = DatabaseCock.getConnection()) {
            System.out.println("Conexión a CockroachDB exitosa!");
        } catch (SQLException e) {
            System.err.println("Error al conectar a CockroachDB:");
            e.printStackTrace();
        }
    }
}