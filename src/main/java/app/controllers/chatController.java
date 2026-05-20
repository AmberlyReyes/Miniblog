package app.controllers;

import app.Main;
import app.entidades.Chat;
import app.entidades.Mensaje;
import app.entidades.Usuario;
import app.servicios.ArticuloServices;
import app.servicios.ChatServices;
import app.servicios.MensajeServices;
import app.servicios.UsuarioServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatController {


    public static Handler getMensajesXChatID = ctx -> {
        try {
            // Obtener el parámetro de la etiqueta
            String chatid = ctx.queryParam("chatid");

            List<Mensaje> mensajes = MensajeServices.getInstance().findByChatId(Long.parseLong(chatid));

            // Convertir a JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Registrar el módulo JavaTimeModule
            String json = mapper.writeValueAsString(mensajes);

            ctx.contentType("application/json");
            ctx.result(json);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Ocurrió un error al obtener los mensajes: " + e.getMessage()));
        }

    };

    public static void listarChats(@NotNull Context ctx) throws Exception {
        Usuario currentUser = ctx.sessionAttribute("USUARIO");
        List<Chat> lista = null;
        if(currentUser.isAdministrador()) {
            lista = ChatServices.getInstance().findAll();
        }else{
            lista = ChatServices.getInstance().findChatsByAuthorUsername(currentUser.getUsername());
        }
        /*if(lista.isEmpty()){
            lista = ChatServices.getInstance().findAll();
        }*/

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Chats");
        modelo.put("lista", lista);
        ctx.render("/dashboard/dashboardChat.html", modelo);
    }

    public static void crearChat(@NotNull Context ctx) throws Exception {
        String autor = ctx.formParam("autor");
        String nombre = ctx.formParam("name");
        String articulo = ctx.formParam("articulo");
        System.out.println(autor);
        Chat chat = chat = new Chat(null, nombre);;
        try{
            if(!autor.isEmpty()){
                chat = new Chat(UsuarioServices.getInstance().find(autor), nombre);
            }
        }catch(Exception e){
            System.out.println("ouu si dio nulo");
        }
        ChatServices.getInstance().crear(chat);
        ctx.sessionAttribute("CHAT", chat);
        if (articulo == null) {
            ctx.redirect("/");
        } else {
            ctx.redirect("/blog/articulo/" + articulo);
        }
    }

    public static void enviarMensaje(@NotNull Context ctx) throws Exception {
        String message = ctx.formParam("mensaje");
        long chatId = Long.parseLong(ctx.formParam("chat"));

        Chat chat = ChatServices.getInstance().find(chatId);
        if (chat != null) {
            Mensaje nuevoMensaje = new Mensaje(message, chat, false, LocalDateTime.now());
            MensajeServices.getInstance().crear(nuevoMensaje);
            chat.setNewMessage(true);
            ChatServices.getInstance().editar(chat);

            Main.notificarNuevaActividad(chatId);

            ctx.status(201).result("Mensaje enviado");
        } else {
            ctx.status(404).result("Chat no encontrado");
        }

    }


    public static void enviarMensajeAdmin(@NotNull Context ctx) throws Exception {
        String message = ctx.formParam("mensaje");
        Chat chat = ChatServices.getInstance().find(ctx.pathParam("id"));

        if (chat != null) {
            // Un administrador envía el mensaje, por eso 'true'
            Mensaje nuevoMensaje = new Mensaje(message, chat, true, LocalDateTime.now());
            MensajeServices.getInstance().crear(nuevoMensaje);

            Main.notificarNuevaActividad(chat.getId());

            ctx.status(201).result("Mensaje enviado");
        } else {
            ctx.status(404).result("Chat no encontrado");
        }
    }


    public static void visualizarChat(@NotNull Context ctx) throws Exception {
        long chatId = Long.parseLong(ctx.pathParam("id"));
        Chat chat = ChatServices.getInstance().find(chatId);
        List<Mensaje> lista = MensajeServices.getInstance().findByChatId(chatId); // Obtiene solo los mensajes del chat
        chat.setNewMessage(false);
        ChatServices.getInstance().editar(chat);
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Chat - " + chat.getNombre());
        modelo.put("chat", chat);
        modelo.put("mensajes", lista);

        // ctx.render("/dashboard/dashboardChat.html", modelo);
        ctx.render("/dashboard/Conversacion.html", modelo);
    }


    public static void eliminarChat(@NotNull Context ctx) throws Exception {
        MensajeServices.getInstance().deleteByChatId(ctx.pathParam("id"));
        ChatServices.getInstance().eliminar(ctx.pathParam("id"));
        ctx.redirect("/crud-chats/");

    }
}
