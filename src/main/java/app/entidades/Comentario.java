package app.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "Comentario")
public class Comentario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String contenido;
    @ManyToOne
    @JoinColumn(name = "autor_username")
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;
    private Date fecha;

    public Comentario(String contenido, Usuario autor, Articulo articulo) {
        this.contenido = contenido;
        this.autor = autor;
        this.articulo = articulo;
        this.fecha = new Date();
    }

    public Comentario() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}