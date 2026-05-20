package app.servicios;

import java.net.*;
import java.io.*;
import org.json.JSONObject;
import java.util.Base64;

public class MicrolinkService {

    public static String obtenerImagenBase64(String url) {
        try {
            String apiUrl = "https://api.microlink.io?url=" + URLEncoder.encode(url, "UTF-8") + "&screenshot=true";
            URL api = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) api.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.lines().reduce("", (acc, line) -> acc + line);
            in.close();

            JSONObject json = new JSONObject(response);
            String imageUrl = json.getJSONObject("data").getJSONObject("screenshot").getString("url");

            // Descargar imagen
            byte[] imageBytes = new URL(imageUrl).openStream().readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            System.out.println("Error obteniendo preview: " + e.getMessage());
            return null;
        }
    }
}
