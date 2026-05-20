package app.controllers;

import app.entidades.Articulo;
import app.entidades.Comentario;
import app.entidades.Etiqueta;
import app.entidades.Usuario;
import app.servicios.ArticuloServices;
import app.servicios.ComentarioServices;
import app.servicios.EtiquetaServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class blogController {

    public static void renderBlog(@NotNull Context ctx) {
        String param = ctx.pathParam("param");
        String name = ctx.pathParam("name");

        Map<String, Object> modelo = new HashMap<>();
        Articulo articulo = null;
        List<Etiqueta> etiquetas = EtiquetaServices.getInstance().findAll();
        modelo.put("ListEtiquetas", etiquetas);

        if (param.equalsIgnoreCase("Articulo")) {
            articulo = ArticuloServices.getInstance().find(name);
            modelo.put("ARTICULOPRIN", articulo);
            List<Articulo> ListArt = DosArticulos(articulo);
            modelo.put("ListArt", ListArt);
            String nombre = "Anonimo";
            try{
                if (articulo.getAutor() != null) {
                    nombre = articulo.getAutor().getNombre();
                }
            } catch (Exception e) {
                System.out.println("Error al obtener el nombre del autor");
            }
            modelo.put("AUTOR", nombre);
            String fechaFormateada = null;
            try{
                SimpleDateFormat formato = new SimpleDateFormat("EEE, dd MMM yyyy");
                fechaFormateada = formato.format(articulo.getFecha());
            } catch (Exception e) {
                System.out.println("Error al obtener el nombre del autor");
            }

            modelo.put("FECHA", fechaFormateada);


            List<Map<String, Object>> comentariosSimplificados = new ArrayList<>();
            try{
                for (Comentario comentario : ComentarioServices.getInstance().findAllByArticulo(articulo)) {
                    Map<String, Object> comentarioMap = new HashMap<>();
                    comentarioMap.put("contenido", comentario.getContenido());
                    comentarioMap.put("autor", comentario.getAutor().getNombre());
                    comentarioMap.put("fecha", comentario.getFecha());
                    comentariosSimplificados.add(comentarioMap);
                }
            }catch (Exception e) {
                System.out.println("Error al obtener el comentarios simplificados");
            }
            modelo.put("listaComentarios", ComentarioServices.getInstance().findAllByArticulo(articulo));


            if (articulo != null) {
                ctx.render("/blog.html", modelo);
            } else {
                ctx.status(404).result("Artículo no encontrado");
            }
        } else if (param.equalsIgnoreCase("Etiqueta")) {
            List<Articulo> articulos = null;
            try{
                articulos = ArticuloServices.getInstance().findAllByEtiqueta(name);

            }catch (Exception e) {

            }
            modelo.put("Etiqueta", param);
            modelo.put("ARTICULOS", articulos);
            ctx.render("ArtListXEtiqueta", modelo);
        }
    }



    private static List<Articulo> DosArticulos(Articulo articulo) {
        List<Articulo> articulos = new ArrayList<>();
        for (Articulo a : ArticuloServices.getInstance().findAll()) {
            int contador = 0;
            if(a.getId() != articulo.getId()) {
                articulos.add(a);
            }
            if(contador > 2){
                return articulos;
            }
        }
        return articulos;
    }


    public static void logout(@NotNull Context ctx) {
        ctx.sessionAttribute("USUARIO", null);
        ctx.redirect("/formulario");
    }

    public static void renderFormulario(@NotNull Context ctx) {
        ctx.render("/formulario.html"); // Renderizar con Thymeleaf
    }


    public static Handler getArticulos = ctx -> {
        try {
            // Obtener el parámetro de la etiqueta
            String etiqueta = ctx.queryParam("etiqueta");

            List<Articulo> articulos;
            if (etiqueta != null && !etiqueta.isEmpty()) {
                // Filtrar artículos por etiqueta
                articulos = ArticuloServices.getInstance().findAllByEtiqueta(etiqueta);
            } else {
                // Obtener todos los artículos si no se proporciona una etiqueta
                articulos = ArticuloServices.getInstance().findAll();
            }

            // Convertir a JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(articulos);

            ctx.contentType("application/json");
            ctx.result(json);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Ocurrió un error al obtener los artículos: " + e.getMessage()));
        }
    };


    public static Handler getALLArticulos = ctx -> {
        List<Articulo> articulos = ArticuloServices.getInstance().findAll();

        // Convertir a JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(articulos);

        ctx.contentType("application/json");
        ctx.result(json);
    };

    public static Handler getNArticulos = ctx -> {
        List<Articulo> articulos = ArticuloServices.getInstance().findAll();

        // Convertir a JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(articulos);

        ctx.contentType("application/json");
        ctx.result(json);
    };

    public static void agregarComentario(Context ctx) {
        // Obtener el ID del artículo desde el formulario
        long articuloId = Long.parseLong(ctx.formParam("articuloId"));
        String contenidoComentario = ctx.formParam("comentario");

        // Obtener el usuario autenticado desde la sesión
        Usuario usuario = ctx.sessionAttribute("USUARIO");

        if (usuario == null) {
            ctx.status(401).result("Usuario no autenticado");
            return;
        }

        // Agregar el comentario al artículo
        try {
            String idString = Long.toString(articuloId); // Convertir long a String
            Articulo articulo = ArticuloServices.getInstance().find(articuloId); // Llamar al método con String
            if (articulo != null) {
                ComentarioServices.getInstance().crear(new Comentario(contenidoComentario, usuario, articulo));
                // Aquí podrías guardar el artículo actualizado en tu base de datos o lista
            } else {
                throw new IllegalArgumentException("Artículo no encontrado");
            }
            ctx.redirect("/blog/Articulo/" + articuloId); // Redirigir a la vista del artículo
        } catch (IllegalArgumentException e) {
            ctx.status(404).result("Artículo no encontrado");
        }
    }

    public static void renderPageN(@NotNull Context ctx) {
        List<Articulo> articulos = ArticuloServices.getInstance().findAll();
        Map<String, Object> modelo = new HashMap<>();
        Articulo articulo = null;
        List<Etiqueta> etiquetas = EtiquetaServices.getInstance().findAll();
        modelo.put("ListEtiquetas", etiquetas);
        int randNum = getRandomNumber(0,articulos.size()-1);
        Articulo art = articulos.get(randNum);
        modelo.put("AriculoDestacado", art);

        modelo.put("ArtDesCuerpo", art.getCuerpo().substring(0,200)+"...");

        String eti = art.getListaEtiquetas().get(0).getEtiqueta();
        try{
            eti +=", " + art.getListaEtiquetas().get(1).getEtiqueta();
        }catch (Exception e){}
        modelo.put("ArtEti", eti);
        modelo.put("ARTICULOS", articulos);
        ctx.render("BlogPagInicio.html", modelo);
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
};


