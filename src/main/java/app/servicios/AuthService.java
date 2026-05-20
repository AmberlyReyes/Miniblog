package app.servicios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuthService {
    public void registrarAutenticacion(String usuario) {
        try {
            String sql = "INSERT INTO autenticaciones (usuario) VALUES (?)";
            try (Connection conn = DatabaseCock.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, usuario);
                pstmt.executeUpdate();
                System.out.println("Autenticacion registrada en CockroachDB para: " + usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar autenticacion en CockroachDB:");
            e.printStackTrace();
        }
    }
}