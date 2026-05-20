package app.entidades;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String mensaje;
    @ManyToOne
    @JoinColumn(name = "chatid_id")
    Chat chatid;
    Boolean Enviado;
    private LocalDateTime fecha;
    //enviado es true cuando fue enviado por el usuario admin, enviado es false cuando es recibido

    public Mensaje() {}

    public Mensaje(String mensaje, Chat chatid, Boolean enviado,LocalDateTime fecha) {
        this.mensaje = mensaje;
        this.chatid = chatid;
        this.fecha=fecha;
        Enviado = enviado;

    }

    public Chat getChatid() {
        return chatid;
    }

    public void setChatid(Chat chatid) {
        this.chatid = chatid;
    }

    public Boolean getEnviado() {
        return Enviado;
    }

    public void setEnviado(Boolean enviado) {
        Enviado = enviado;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }


}
