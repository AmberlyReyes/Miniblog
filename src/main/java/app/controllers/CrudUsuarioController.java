package app.controllers;

import app.entidades.Foto;
import app.entidades.Usuario;
import app.servicios.FotoServices;
import app.servicios.UsuarioServices;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrudUsuarioController {

    public static void listar(@NotNull Context ctx) throws Exception {
        List<Usuario> lista = UsuarioServices.getInstance().consultaNativa();
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Listado de Usuarios");
        modelo.put("lista", lista);
        ctx.render("dashboard/dashboardUsuario.html", modelo);
    }

    public static void crearUsuarioForm(@NotNull Context ctx) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Creación Usuario");
        modelo.put("accion", "/crud-simple/crear");
        ctx.render("/crud-tradicional/crearEditarVisualizar.html", modelo);
    }

    public static void procesarCreacionUsuario(@NotNull Context ctx) throws Exception {
        String username = ctx.formParam("username");
        String nombre = ctx.formParam("nombre");
        String password = ctx.formParam("password");
        Foto foto = null;
        try{
            UploadedFile uploadedFile = ctx.uploadedFile("foto");
            if (uploadedFile != null) {
                byte[] bytes = uploadedFile.content().readAllBytes();
                String encodedString = Base64.getEncoder().encodeToString(bytes);
                foto = new Foto(uploadedFile.filename(), uploadedFile.contentType(), encodedString);
                FotoServices.getInstancia().crear(foto);
            }

        }catch (Exception e){
            System.out.println("foto aint working");
        }

        String admincheck = ctx.formParam("administrador");
        String autorcheck = ctx.formParam("autor");

        boolean administrador = false;
        boolean autor = false;
        if(admincheck != null){
            administrador = true;
        }
        if(autorcheck != null){
            autor = true;
        }

        Usuario usuario = new Usuario(username, nombre, password, administrador, autor, foto);
        UsuarioServices.getInstance().crear(usuario);
        ctx.redirect("/crud-simple/");
    }

    public static void visualizarUsuario(@NotNull Context ctx) throws Exception {
        Usuario usuario = UsuarioServices.getInstance().find(ctx.pathParam("username"));
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Visualizando a " + usuario.getUsername());
        modelo.put("usuario", usuario);
        modelo.put("visualizar", true);
        modelo.put("editaruser", true);
        modelo.put("accion", "/crud-simple/");
        ctx.render("/crud-tradicional/crearEditarVisualizar.html", modelo);
    }

    public static void editarUsuarioForm(@NotNull Context ctx) throws Exception {
        Usuario usuario = UsuarioServices.getInstance().find(ctx.pathParam("username"));
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Editando a " + usuario.getUsername());
        modelo.put("usuario", usuario);
        modelo.put("editaruser", true);
        modelo.put("accion", "/crud-simple/editar");
        ctx.render("/crud-tradicional/crearEditarVisualizar.html", modelo);
    }

    public static void procesarEditarUsuario(@NotNull Context ctx) throws Exception {
        String username = ctx.formParam("username");
        String nombre = ctx.formParam("nombre");
        String password = ctx.formParam("password");
        String admincheck = ctx.formParam("administrador");
        String autorcheck = ctx.formParam("autor");

        boolean administrador = false;
        boolean autor = false;
        if(admincheck != null){
            administrador = true;
        }
        if(autorcheck != null){
            autor = true;
        }

        Foto foto = null;
        Foto oldfoto = null;
        Boolean FotoSubida = false;

        try{
            UploadedFile uploadedFile = ctx.uploadedFile("foto");
            if (uploadedFile != null) {
                byte[] bytes = uploadedFile.content().readAllBytes();
                String encodedString = Base64.getEncoder().encodeToString(bytes);
                foto = new Foto(uploadedFile.filename(), uploadedFile.contentType(), encodedString);
                FotoServices.getInstancia().crear(foto);
                Usuario CurrentUser = UsuarioServices.getInstance().find(username);
                oldfoto = CurrentUser.getFoto();
                FotoSubida = true;
            }
        }catch (Exception e){
            System.out.println("error");
        }

        Usuario temp = new Usuario(username, nombre, password, administrador, autor, foto);
        UsuarioServices.getInstance().editar(temp);
        try{
            if (oldfoto != null && FotoSubida) {
                //FotoServices.getInstancia().eliminar(oldfoto.getId());
            }
        }catch (Exception e){
            System.out.println("aint old foto working");
        }
        ctx.redirect("/crud-simple/");
    }

    public static void eliminarUsuario(@NotNull Context ctx) throws Exception {
        Usuario usuario = UsuarioServices.getInstance().find(ctx.pathParam("username"));
        UsuarioServices.getInstance().eliminar(ctx.pathParam("username"));
        FotoServices.getInstancia().eliminar(usuario.getFoto().getId());
        ctx.redirect("/crud-simple/");
    }
}
