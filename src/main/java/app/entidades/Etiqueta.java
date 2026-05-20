package app.entidades;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "Etiqueta")
public class Etiqueta implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String etiqueta;

    public Etiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Etiqueta() {

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }
}
