package app.servicios;

import app.entidades.Mensaje;
import jakarta.persistence.EntityManager;

import java.util.List;

public class MensajeServices extends GestionDb<Mensaje> {
    private static MensajeServices instancia;
    private MensajeServices() {
        super(Mensaje.class);
    }

    public static MensajeServices getInstance(){
        if(instancia==null){
            instancia = new MensajeServices();
        }
        return instancia;
    }

    public List<Mensaje> findByChatId(long chatId) {
        EntityManager em = getEntityManager();
        List<Mensaje> mensajes = null;

        try {
            mensajes = em.createQuery("SELECT m FROM Mensaje m WHERE m.chatid.id = :chatId ORDER BY m.id ASC", Mensaje.class)
                    .setParameter("chatId", chatId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return mensajes;
    }

    public void deleteByChatId(String chatId) {
        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin(); // Begin transaction


            int deletedCount = em.createQuery("DELETE FROM Mensaje m WHERE m.chatid.id = :chatId")
                    .setParameter("chatId", chatId)
                    .executeUpdate();

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

}
