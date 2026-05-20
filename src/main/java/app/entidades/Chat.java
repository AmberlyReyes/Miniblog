package app.entidades;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @ManyToOne
    @JoinColumn(name = "autor_username")
    Usuario autor;
    String nombre;
    boolean newMessage;
    Date fecha;

    public Chat(Usuario usuario, String nombre) {
        this.autor = usuario;
        this.nombre = nombre;
        newMessage = false;
        fecha = new Date(System.currentTimeMillis());
    }

    public Chat() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario usuario) {
        this.autor = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
