package app.util;

import app.entidades.Usuario;
import app.servicios.UsuarioServices;
import io.javalin.http.Context;
import io.jsonwebtoken.*;

public class JwtUtil {

    private static final String SECRET_KEY = "clave_secreta_super_segura";

    public static Usuario obtenerUsuarioDesdeJWT(Context ctx) {
        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;

        String token = auth.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            return UsuarioServices.getInstance().find(username);

        } catch (Exception e) {
            return null;
        }
    }

    public static String crearToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getUsername())
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }
}
