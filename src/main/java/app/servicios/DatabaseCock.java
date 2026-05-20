package app.servicios;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseCock {
    private static DataSource dataSource;

    static {
        String url = System.getenv("JDBC_DATABASE_URL");
        if (url != null && !url.isEmpty()) {
            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setUrl(url);
            dataSource = ds;

            // Verificar y crear la tabla si no existe
            crearTablaSiNoExiste();
        } else {
            System.err.println("Advertencia: JDBC_DATABASE_URL no está configurada. No se registrarán autenticaciones en CockroachDB.");
            dataSource = null;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("No se pudo conectar a CockroachDB: JDBC_DATABASE_URL no está configurada.");
        }
        return dataSource.getConnection();
    }

    private static void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS autenticaciones (" +
                "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "usuario TEXT NOT NULL, " +
                "fecha_hora TIMESTAMP NOT NULL DEFAULT now()" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'autenticaciones' creada");
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla 'autenticaciones':");
            e.printStackTrace();
        }
    }
}