package app.servicios;

import app.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;


public class UsuarioServices extends GestionDb<Usuario> {
    private static UsuarioServices instancia;
    private UsuarioServices() {
        super(Usuario.class);
    }

    public static UsuarioServices getInstance(){
        if(instancia==null){
            instancia = new UsuarioServices();
        }
        return instancia;
    }

    public List<Usuario> findAllByUsername(String username){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select e from Usuario e where e.username like :username");
        query.setParameter("username", username + "%");
        return query.getResultList();
    }
    public List<Usuario> consultaNativa(){
        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery("select * from Usuario ", Usuario.class);
        //query.setParameter("nombre", apellido+"%");
        return query.getResultList();
    }

}
