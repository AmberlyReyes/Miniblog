package app;

import app.controllers.*;
import app.entidades.*;
import app.servicios.*;
import io.javalin.Javalin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;


import io.javalin.http.UploadedFile;
import jakarta.servlet.http.Cookie;

import io.javalin.rendering.template.JavalinThymeleaf;
import io.javalin.security.RouteRole;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;
import io.javalin.websocket.WsContext;


public class Main {
    private static String modoConexion = "";

    enum Rules implements RouteRole {
        ANONYMOUS,
        USER,
    }

    private static Map<Long, Set<WsContext>> chatConnections = new ConcurrentHashMap<>();



    public static void main(String[] args) {
        String mensaje = "Software ORM - JPA";
        System.out.println(mensaje);
        if (args.length >= 1) {
            modoConexion = args[0];
            System.out.println("Modo de Operacion: " + modoConexion);
        }

        //Iniciando la base de datos.
        if (modoConexion.isEmpty()) {
            BootStrapServices.getInstancia().init();
        }


        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");  // ⚠️ SIN la barra inicial "/templates/"
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);
        templateEngine.setTemplateResolver(resolver);

        // Crear la aplicación Javalin con el motor de plantillas
        var app = Javalin.create(config -> {
            config.staticFiles.add("/publico"); // Archivos estáticos
            config.fileRenderer(new JavalinThymeleaf(templateEngine)); // Configurar Thymeleaf

            config.router.apiBuilder(() -> {
                ApiBuilder.path("/crud-simple", () -> {
                    ApiBuilder.get("/", CrudUsuarioController::listar);
                    ApiBuilder.get("/crear", CrudUsuarioController::crearUsuarioForm);
                    ApiBuilder.post("/crear", CrudUsuarioController::procesarCreacionUsuario);
                    ApiBuilder.get("/visualizar/{username}", CrudUsuarioController::visualizarUsuario);
                    ApiBuilder.get("/editar/{username}", CrudUsuarioController::editarUsuarioForm);
                    ApiBuilder.post("/editar", CrudUsuarioController::procesarEditarUsuario);
                    ApiBuilder.get("/eliminar/{username}", CrudUsuarioController::eliminarUsuario);
                });
                ApiBuilder.path("/crud-articulo", () -> {
                    ApiBuilder.get("/", CrudAritculoController::listar);
                    ApiBuilder.get("/crear", CrudAritculoController::crearArticuloForm);
                    ApiBuilder.post("/crear", CrudAritculoController::procesarCreacionArticulo);
                    ApiBuilder.get("/visualizar/{id}", CrudAritculoController::visualizarArticulo);
                    ApiBuilder.get("/editar/{id}", CrudAritculoController::editarArticuloForm);
                    ApiBuilder.post("/editar/", CrudAritculoController::procesarEditarArticulo);
                    ApiBuilder.get("/eliminar/{id}", CrudAritculoController::eliminarArticulo);
                });
                path("/fotos",() -> {
                    get(ctx -> {
                        ctx.redirect("/fotos/listar");
                    });
                    get("/listar", FotoController::listarFotos);
                    post("/procesarFoto", FotoController::procesarFotos);
                    get("/visualizar/{id}", FotoController::visualizarFotos);
                    get("/eliminar/{id}", FotoController::eliminarFotos);
                });
                path("/crud-chats",() -> {
                    get("/", chatController::listarChats);
                    get("/{id}", chatController::visualizarChat);
                    get("/eliminar/{id}", chatController::eliminarChat);
                    post("/{id}/send", chatController::enviarMensajeAdmin);

                });
            });
        }).start(7071);



        app.ws("/ws/chat/{chatId}", ws -> {
            ws.onConnect(ctx -> {
                Long chatId = Long.parseLong(ctx.pathParam("chatId"));
                chatConnections.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(ctx);
                System.out.println("Nueva conexión para chat: " + chatId);
            });

            ws.onClose(ctx -> {
                Long chatId = Long.parseLong(ctx.pathParam("chatId"));
                Set<WsContext> connections = chatConnections.get(chatId);
                if (connections != null) {
                    connections.remove(ctx);
                    if (connections.isEmpty()) {
                        chatConnections.remove(chatId);
                    }
                }
            });
        });

        app.before(ctx -> {
            if (ctx.sessionAttribute("USUARIO") == null) { // Si no hay sesión activa
                Cookie[] cookies = ctx.req().getCookies();

                if (cookies != null) {
                    Optional<Cookie> cookieOpt = Arrays.stream(cookies)
                            .filter(cookie -> "usuarioRecordado".equals(cookie.getName()))
                            .findFirst();

                    if (cookieOpt.isPresent()) {
                        String username = EncriptarUser.desencriptar(cookieOpt.get().getValue());
                        Usuario usuario = UsuarioServices.getInstance().find(username);

                        if (usuario != null) {
                            ctx.sessionAttribute("USUARIO", usuario); // Restaura la sesión
                        }
                    }
                }
            }
        });

        // Definición de rutas principales
       app.get("/articulos", blogController.getArticulos);
       app.get("/articul", blogController.getNArticulos);

       app.post("/crud-chat/", ctx ->{
           String url = ctx.url();
           String name = ctx.formParam("name");
           String autor = ctx.formParam("autor");
           Chat chat = new Chat(null, name);
           ctx.sessionAttribute("CHAT", name);
           ctx.redirect(url);

       });
        app.post("/newchat", chatController::crearChat);
        app.post("/send", chatController::enviarMensaje);
        app.get("/mensajes", chatController.getMensajesXChatID);



        app.get("/art", ctx -> {
            String etiqueta = ctx.queryParam("etiqueta");
            String paginaParam = ctx.queryParam("pagina");
            int pagina = 1;

            if (paginaParam != null && !paginaParam.isEmpty()) {
                try {
                    pagina = Integer.parseInt(paginaParam);
                } catch (NumberFormatException e) {
                    pagina = 1; // Si el valor no es válido, usar la página 1
                }
            }

            int articulosPorPagina = 5;

            List<Articulo> articulos;

            if (etiqueta != null && !etiqueta.isEmpty()) {
                articulos = ArticuloServices.getInstance().findAllByEtiqueta(etiqueta);
            } else {
                articulos = ArticuloServices.getInstance().findAll();
            }

            int inicio = (pagina - 1) * articulosPorPagina;
            int fin = Math.min(inicio + articulosPorPagina, articulos.size());

            boolean hayMasArticulos = fin < articulos.size();

            // Calcular el total de artículos
            int totalArticulos = articulos.size();

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("articulos", articulos.subList(inicio, fin));
            respuesta.put("hayMasArticulos", hayMasArticulos);
            respuesta.put("totalArticulos", totalArticulos); // Agregar el total de artículos

            ctx.json(respuesta);
        });



        app.get("/", ctx -> ctx.redirect("/blog/"));

        app.get("/blog/inicio", blogController :: renderPageN);
        app.get("/blog", ctx -> ctx.redirect("/blog/inicio"));
        app.get("/blog/{param}/{name}", blogController::renderBlog);
        app.get("/formulario", ctx -> ctx.render("/formulario.html"));
        app.get("/registro", ctx -> ctx.render("registro.html"));
        app.get("/menu", ctx -> ctx.render("/menu.html"));
        app.post("/comentar", blogController::agregarComentario);
        app.get("/login", ctx -> ctx.redirect("/formulario"));



        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.removeCookie("usuarioRecordado");
            ctx.redirect("/formulario");
        });

        app.before("/crud-*", ctx -> {
            if (!SessionCheckAutor(ctx)) {
                ctx.redirect("/");
            }
        });

        app.before("/crud-simple*", ctx -> {
            if (!SessionCheckAdmin(ctx)) {
                ctx.redirect("/");
            }
        });

        app.before("/fotos*", ctx -> {
            if (!SessionCheckAdmin(ctx)) {
                ctx.redirect("/");
            }
        });



        app.post("/autenticar", ctx -> {
            String usuario = ctx.formParam("usuario");
            String contrasena = ctx.formParam("password");
            boolean recordar = "on".equals(ctx.formParam("recordar"));
            Usuario user = new Usuario(usuario, null, contrasena, false, false);

            if (validarUsuario(user)) {
                user = UsuarioServices.getInstance().find(user.getUsername());
                ctx.sessionAttribute("USUARIO", user);
                ctx.sessionAttribute("NAME", null);


                // Registrar la autenticación en la tabla externa (CockroachDB)
                System.out.println("Intentando registrar autenticacion en CockroachDB para: " + user.getUsername());
                AuthService authService = new AuthService();
                authService.registrarAutenticacion(user.getUsername());

                if (recordar) {
                    String datosEncriptados = EncriptarUser.encriptar(user.getUsername());
                    Cookie cookie = new Cookie("usuarioRecordado", datosEncriptados);
                    cookie.setMaxAge(7 * 24 * 60 * 60); // Expira en 1 semana
                    cookie.setPath("/");
                    ctx.res().addCookie(cookie);
                }

                ctx.redirect("/blog/");
            } else {
                ctx.redirect("/formulario?error=credenciales"); // Redirigir con parámetro de error
            }
        });

        app.post("/registro", ctx -> {
            String nombre = ctx.formParam("nombre");
            String username = ctx.formParam("username");
            String contrasena = ctx.formParam("password");
            Foto foto = null;
            try{
                UploadedFile uploadedFile = ctx.uploadedFile("foto");
                if (uploadedFile != null) {
                    byte[] bytes = uploadedFile.content().readAllBytes();
                    String encodedString = Base64.getEncoder().encodeToString(bytes);
                    foto = new Foto(uploadedFile.filename(), uploadedFile.contentType(), encodedString);
                }

            }catch (Exception e){
                System.out.println("foto aint working");
            }


            if (nombre == null || username == null || contrasena == null) {
                ctx.status(400);
                ctx.result("Llena todos los campos");
                return;
            }
            try{
                if(foto != null){
                    FotoServices.getInstancia().crear(foto);
                }
            }catch (Exception e){

            }
            Usuario user = new Usuario(nombre, username, contrasena, false, false, foto);
            UsuarioServices.getInstance().crear(user);

            ctx.redirect("/blog/");
        });

        if(ArticuloServices.getInstance().findAll().isEmpty()) {
            //startDB();
            System.out.println("Articulos no encontrados");
        }

        app.post("/logout", ctx -> {
            ctx.sessionAttribute("USUARIO", null); // Eliminar sesión

            Cookie cookie = new Cookie("usuarioRecordado", "");
            cookie.setMaxAge(0); // Expirar inmediatamente
            cookie.setPath("/");
            ctx.res().addCookie(cookie);

            ctx.redirect("/login");
        });
    }
    private static boolean SessionCheck(Context ctx) {
        Usuario user = ctx.sessionAttribute("USUARIO");

        if (user == null) {
            Cookie[] cookies = ctx.req().getCookies();

            if (cookies != null) {
                Optional<Cookie> cookieOpt = Arrays.stream(cookies)
                        .filter(cookie -> "usuarioRecordado".equals(cookie.getName()))
                        .findFirst();

                if (cookieOpt.isPresent()) {
                    String username = EncriptarUser.desencriptar(cookieOpt.get().getValue());
                    Usuario usuario = UsuarioServices.getInstance().find(username);

                    if (usuario != null) {
                        ctx.sessionAttribute("USUARIO", usuario);
                        return true;
                    }
                }
            }
            return false; // No hay sesión ni cookie válida
        }

        return true; // Usuario ya tenía sesión activa
    }


    private static boolean SessionCheckAutor(Context ctx) {
        Usuario user = ctx.sessionAttribute("USUARIO");
        if (user != null) {
            ctx.sessionAttribute("USUARIO", user);
            if(user.isAutor() || user.isAdministrador()){
                return true;
            }
        }
        return false; // No hay sesión ni cookie válida
    }

    private static boolean SessionCheckAdmin(Context ctx) {
        Usuario user = ctx.sessionAttribute("USUARIO");
        if (user != null) {
            ctx.sessionAttribute("USUARIO", user);
            if(user.isAdministrador()){
                return true;
            }
        }
        return false; // No hay sesión ni cookie válida
    }




    public static boolean validarUsuario(Usuario user) {
        List<Usuario> misUsuarios = UsuarioServices.getInstance().findAll();

        if (misUsuarios != null && !misUsuarios.isEmpty()) {
            for (Usuario u : misUsuarios) {
                if (user.getUsername().equals(u.getUsername()) &&
                        u.getPassword().equals(user.getPassword())) {
                    return true;
                }
            }
        }
        return false;
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 7000; //Retorna el puerto por defecto en caso de no estar en Heroku.
    }

    public static String getModoConexion() {
        return modoConexion;
    }


    private static void startDB() {
        Usuario Admin = new Usuario("admin", "adminis", "admin", true, false);

        UsuarioServices.getInstance().crear(Admin);
        EtiquetaServices.getInstance().crear(new Etiqueta("Mundo"));
        EtiquetaServices.getInstance().crear(new Etiqueta("RepDom"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Tecnologia"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Diseno"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Cultura"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Negocios"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Politica"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Opinion"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Ciencia"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Salud"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Estilo"));
        EtiquetaServices.getInstance().crear(new Etiqueta("Viajes"));
        Usuario anonimo = new Usuario("Anonimo", "Anonimo", "0564684", false, true);
        UsuarioServices.getInstance().crear(anonimo);
        List<Etiqueta> eti = EtiquetaServices.getInstance().findAll();
        ArticuloServices.getInstance().crear(new Articulo("Deportes hoy en dia", "Yvelisse Altagracia Reyes, una mujer de 66 años, denunció el pasado viernes ser víctima de una estafa inmobiliaria relacionada con el proyecto Bávaro Victoriana Residences de la empresa Novasco Real Estate, ubicado en Verón, en la carretera vieja de Bávaro.\n" +
                "\n" +
                "Reyes relató a Diario Libre cómo en 2023 apartó un apartamento en planos por un costo de US$49,000 en la primera etapa del proyecto. Sin embargo, tras realizar pagos que superaron los RD$1.2 millones, se enteró en agosto de 2024 que el proyecto no seguiría adelante, cuando el apartamento debía entregarse en marzo de 2025."
                , anonimo, eti));
        ArticuloServices.getInstance().crear(new Articulo("Trump hace tal cosa", "El ministro de Energía y Minas, Joel Santos, informó este domingo sobre los avances en la exploración de tierras raras en el país, un recurso estratégico clave para el desarrollo tecnológico y económico global.  Explicó que en 2026 se espera determinar la presencia de estos minerales mediante estudios de mineralogía, sondeos y calicatas para conocer su profundidad y cantidad. La primera declaración de reservas podría realizarse en el citado año, una vez completados los análisis de prefactibilidad.\n" +
                "\n" +
                "Las tierras raras, derivadas de la bauxita en la región Sur del país, son esenciales para la fabricación de dispositivos tecnológicos como equipos médicos y teléfonos celulares."
                , anonimo, eti));
        ArticuloServices.getInstance().crear(new Articulo("Perro se cae de una 3ra y sobrevive", "El Consejo Unificado de las Empresas Distribuidoras (CUED) defendió la transparencia, legalidad y beneficios del proceso de licitación llevado a cabo por Edesur Dominicana para la contratación de subagentes recaudadores. Estos subagentes se encargarán del cobro de facturas a clientes postpago y la venta de recargas de energía a clientes prepago.\n" +
                "\n" +
                "Celso Marranzini, presidente del CUED, afirmó que este proceso cumple con la Ley 340-06 sobre Compras y Contrataciones de Bienes, Servicios, Obras y Concesiones. Según explicó en una nota de prensa de la institución,  la licitación es transparente, competitiva y alineada con las normativas vigentes, garantizando eficiencia y equidad en la selección de los prestadores de este servicio esencial para los clientes de Edesur."
                , anonimo, eti));
        UsuarioServices.getInstance().crear(new Usuario("user1", "Usuario Normal", "user123", false, false));
        UsuarioServices.getInstance().crear(new Usuario("autor1", "Autor", "autor123", false, true));


    }

    public static void notificarNuevaActividad(Long chatId) {
        Set<WsContext> connections = chatConnections.get(chatId);
        if (connections != null) {
            String notificacion = "{\"type\": \"refresh_chat\"}";
            connections.forEach(ctx -> {
                try {
                    ctx.send(notificacion);
                } catch (Exception e) {
                    System.err.println("Error enviando notificación: " + e.getMessage());
                }
            });
        }
    }




}