package app.servicios;

import app.entidades.Chat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ChatServices extends GestionDb<Chat> {
    private static ChatServices instancia;

    public ChatServices() {
        super(Chat.class);
    }

    public static ChatServices getInstance(){
        if(instancia==null){
            instancia = new ChatServices();
        }
        return instancia;
    }

    public List<Chat> findChatsByAuthorUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Chat> query = em.createQuery(
                    "SELECT c FROM Chat c WHERE c.autor.username = :username",
                    Chat.class
            );
            query.setParameter("username", username);
            return query.getResultList();
        } finally {
            em.close();
        }
    }




}
