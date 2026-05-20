package app.controllers;

import app.entidades.Articulo;
import app.entidades.Etiqueta;
import app.entidades.Usuario;
import app.servicios.ArticuloServices;
import app.servicios.EtiquetaServices;
import app.servicios.UsuarioServices;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrudAritculoController {

    public static void listar(@NotNull Context ctx) throws Exception {
        Usuario user = ctx.sessionAttribute("USUARIO");
        List<Articulo> lista = null;
        try{
            if(user != null) {
                if(user.isAdministrador()) {
                    //no es admin
                    lista = ArticuloServices.getInstance().findAll();
                }else{
                    lista = ArticuloServices.getInstance().findAllByAutor(user.getUsername());
                }
            }
            if(lista == null) {
                lista = new ArrayList<>();
            }
        }catch (Exception e) {
            System.out.println("Usuario no encontrado esta raro eso chekeate manito");
        }
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Listado de Articulos");
        modelo.put("lista", lista);
        ctx.render("dashboard/dashboardArticulos.html", modelo);

    }

    public static void crearArticuloForm(@NotNull Context ctx) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Creación Articulo");
        modelo.put("accion", "/crud-articulo/crear");
        List<Etiqueta> lista = EtiquetaServices.getInstance().findAll();
        modelo.put("listaEtiquetas", lista);
        ctx.render("/crud-tradicional/CrudArticulo.html", modelo);
    }

    public static void procesarCreacionArticulo(@NotNull Context ctx) throws Exception {
        String titulo = ctx.formParam("titulo");
        String cuerpo = ctx.formParam("cuerpo");
        String autor = ctx.formParam("autor");
        String etiquetas = ctx.formParam("listaEtiquetas");
        List<Etiqueta> ListEtiquetas = StringToEtiList(etiquetas);
        Usuario autore = UsuarioServices.getInstance().find(autor);
        ArticuloServices.getInstance().crear(new Articulo(titulo, cuerpo, autore, ListEtiquetas));
        ctx.redirect("/crud-articulo/");
    }


    public static void visualizarArticulo(@NotNull Context ctx) throws Exception {
        Articulo articulo = ArticuloServices.getInstance().find(ctx.pathParam("id"));
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Visualizar Articulo ID: " + articulo.getId());
        modelo.put("articulo", articulo);
        modelo.put("autor", articulo.getAutor());
        List<Etiqueta> lista = articulo.getListaEtiquetas();
        String etiquetas = ListToString(lista);
        modelo.put("listaEtiquetas", etiquetas);
        modelo.put("visualizar", true);
        modelo.put("accion", "/crud-articulo/");
        ctx.render("/crud-tradicional/CrudArticulo.html", modelo);
    }

    public static void editarArticuloForm(@NotNull Context ctx) throws Exception {
        Articulo articulo = ArticuloServices.getInstance().find(ctx.pathParam("id"));
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Editar Articulo ID " + articulo.getId());
        modelo.put("articulo", articulo);
        List<Etiqueta> lista = articulo.getListaEtiquetas();
        String etiquetas = ListToString(lista);
        modelo.put("listaEtiquetas", etiquetas);
        modelo.put("autor", articulo.getAutor());
        modelo.put("accion", "/crud-articulo/editar");
        ctx.render("/crud-tradicional/CrudArticulo.html", modelo);
    }

    public static void procesarEditarArticulo(@NotNull Context ctx) throws Exception {
        Articulo articulo = ArticuloServices.getInstance().find(ctx.formParam("id"));
        String titulo = ctx.formParam("titulo");
        String cuerpo = ctx.formParam("cuerpo");
        String autor = ctx.formParam("autor");
        String etiquetas = ctx.formParam("listaEtiquetas");
        List<Etiqueta> ListEtiquetas = StringToEtiList(etiquetas);
        Usuario autore = UsuarioServices.getInstance().find(autor);
        ArrayList<Etiqueta> listaEtiquetas = new ArrayList<>();
        articulo.setTitulo(titulo);
        articulo.setCuerpo(cuerpo);
        articulo.setAutor(autore);
        articulo.setListaEtiquetas(ListEtiquetas);
        ArticuloServices.getInstance().editar(articulo);
        ctx.redirect("/crud-articulo/");
    }

    public static void eliminarArticulo(@NotNull Context ctx) throws Exception {
        ArticuloServices.getInstance().eliminar(ctx.pathParam("id"));
        ctx.redirect("/crud-articulo/");
    }


    private static List<Etiqueta> StringToEtiList(String etisuscia) {
        List<Etiqueta> listaEtiquetas = new ArrayList<>();
        String[] etilimpias = limpiarYDividir(etisuscia);
        List<Etiqueta> lista = EtiquetaServices.getInstance().findAll();
        boolean encontrado = false;
        for (String etiqueta : etilimpias) {
            for (Etiqueta item : lista) {
                if (item.getEtiqueta().equalsIgnoreCase(etiqueta)) {
                    encontrado = true;
                    listaEtiquetas.add(item);
                    break;
                }
            }
            if (!encontrado) {
                Etiqueta temp = new Etiqueta(etiqueta);
                EtiquetaServices.getInstance().crear(temp);
                listaEtiquetas.add(temp);
            }
            encontrado = false;
        }
        return listaEtiquetas;
    }

    private static String ListToString(List<Etiqueta> etisuscia) {
        String lista = "";
        for (Etiqueta item : etisuscia) {
            lista += item.getEtiqueta() + ", ";
        }
        return lista;
    }

    public static String[] limpiarYDividir(String texto) {
        // Eliminar todos los signos de puntuación no deseados (como comas y puntos)
        String textoLimpio = texto.replaceAll("[^a-zA-Z\\s]", "");

        // Dividir el texto en un array de palabras usando el espacio como delimitador
        String[] palabras = textoLimpio.split("\\s+");

        return palabras;
    }


}
