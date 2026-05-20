package app.servicios;

import app.entidades.Articulo;
import app.entidades.Etiqueta;
import app.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

public class ArticuloServices extends GestionDb<Articulo> {
    private static ArticuloServices instancia;
    public ArticuloServices() {
        super(Articulo.class);
    }

    public static ArticuloServices getInstance(){
        if(instancia==null){
            instancia = new ArticuloServices();
        }
        return instancia;
    }

    public List<Articulo> findAllByID(String idItem){
        EntityManager em = getEntityManager();
        Long id = Long.valueOf(idItem);
        Query query = em.createQuery("select e from Articulo e where e.id = :id");
        query.setParameter("id", id + "%");
        return query.getResultList();
    }
    public List<Articulo> consultaNativa(){
        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery("select * from Articulo ", Articulo.class);
        //query.setParameter("nombre", apellido+"%");
        return query.getResultList();
    }

    public List<Articulo> findAllByEtiqueta(String nombreEtiqueta) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery(
                "SELECT a FROM Articulo a " +
                        "JOIN a.listaEtiquetas e " + // Join con la lista de etiquetas
                        "WHERE e.etiqueta = :nombreEtiqueta" // Filtra por el nombre
        );
        query.setParameter("nombreEtiqueta", nombreEtiqueta);
        return query.getResultList();
    }

    public List<Articulo> findAllByAutor(String username) {
    EntityManager em = getEntityManager();
    Query query = em.createQuery(
        "SELECT a FROM Articulo a WHERE a.autor.username = :username"
    );
    query.setParameter("username", username);
    return query.getResultList();
}
    public List<Articulo> findAll(int pagina, int articulosPorPagina) {
        List<Articulo> todosLosArticulos = findAll(); // Obtener todos los artículos
        int inicio = (pagina - 1) * articulosPorPagina;
        int fin = Math.min(inicio + articulosPorPagina, todosLosArticulos.size());
        return todosLosArticulos.subList(inicio, fin);
    }
}
