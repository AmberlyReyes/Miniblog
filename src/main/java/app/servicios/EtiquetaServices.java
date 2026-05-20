package app.servicios;

import app.entidades.Articulo;
import app.entidades.Etiqueta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;

import java.util.List;

public class EtiquetaServices extends GestionDb<Etiqueta>{
    private static EtiquetaServices instancia;

    EtiquetaServices() {
        super(Etiqueta.class);
    }

    public static EtiquetaServices getInstance(){
        if(instancia==null){
            instancia = new EtiquetaServices();
        }
        return instancia;
    }

    public List<Etiqueta> findAllByID(String idItem){
        EntityManager em = getEntityManager();
        Long id = Long.valueOf(idItem);
        Query query = em.createQuery("select e from Etiqueta e where e.id = :id");
        query.setParameter("id", id + "%");
        return query.getResultList();
    }


    public List<Etiqueta> findAllByNOM(String idItem){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select e from Etiqueta e where e.etiqueta like :idItem");
        query.setParameter("idItem", idItem + "%");
        return query.getResultList();
    }
    public List<Etiqueta> consultaNativa(){
        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery("select * from Etiqueta ", Articulo.class);
        //query.setParameter("nombre", apellido+"%");
        return query.getResultList();
    }
    public List<Etiqueta> findEtiquetasByArticuloId(Long articuloId) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("SELECT a.listaEtiquetas FROM Articulo a WHERE a.id = :articuloId");
        query.setParameter("articuloId", articuloId);
        return query.getResultList();
    }

}
