package app.servicios;

import app.entidades.Articulo;
import app.entidades.Comentario;
import app.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.metamodel.model.domain.internal.ArrayTupleType;

import java.util.List;

public class ComentarioServices extends GestionDb<Comentario>{
    private static ComentarioServices instancia;

    public ComentarioServices() {
        super(Comentario.class);
    }

    public static ComentarioServices getInstance(){
        if(instancia==null){
            instancia = new ComentarioServices();
        }
        return instancia;
    }

    public List<Comentario> findAllByArticulo(Articulo articulo) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("SELECT e FROM Comentario e WHERE e.articulo = :articulo");
        query.setParameter("articulo", articulo);
        return query.getResultList();
    }

    public List<Comentario> consultaNativa(){
        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery("select * from Comentario ", Comentario.class);
        //query.setParameter("nombre", apellido+"%");
        return query.getResultList();
    }

}
