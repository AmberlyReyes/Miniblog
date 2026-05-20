package app.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import app.servicios.EtiquetaServices;
import app.servicios.UsuarioServices;
import jakarta.persistence.*;

@Entity
@Table(name = "Articulo")
public class Articulo implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        private  String titulo;
        @Lob
        @Column(columnDefinition = "TEXT")
        private String cuerpo;
        private Date fecha;
        @ManyToOne
        @JoinColumn(name = "autor_username")
        private Usuario autor;
        @ManyToMany
        private  List<Etiqueta> listaEtiquetas;

        public Articulo(String titulo, String cuerpo, Usuario autor, List<Etiqueta> listaEtiquetas) {
                this.titulo = titulo;
                this.cuerpo = cuerpo;
                this.autor = autor;
                if(autor == null) {
                        this.autor = UsuarioServices.getInstance().find("Anonimo");
                }
                this.fecha = new Date();
                this.listaEtiquetas = listaEtiquetas;
        }

        public Articulo(String titulo, String cuerpo, List<Etiqueta> listaEtiquetas) {
                this.titulo = titulo;
                this.cuerpo = cuerpo;
                this.fecha = new Date();
                this.autor = UsuarioServices.getInstance().find("Anonimo");
                ; //add later: current usuario
                this.listaEtiquetas = listaEtiquetas;

        }

        public Articulo() {

        }

        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public String getTitulo() {
                return titulo;
        }

        public void setTitulo(String titulo) {
                this.titulo = titulo;
        }

        public String getCuerpo() {
                return cuerpo;
        }

        public void setCuerpo(String cuerpo) {
                this.cuerpo = cuerpo;
        }

        public Usuario getAutor() {
                return autor;
        }

        public void setAutor(Usuario autor) {
                this.autor = autor;
        }

        public Date getFecha() {
                return fecha;
        }

        public void setFecha(Date fecha) {
                this.fecha = fecha;
        }


        public List<Etiqueta> getListaEtiquetas() {
            return EtiquetaServices.getInstance().findEtiquetasByArticuloId(this.id);
        }

        public void setListaEtiquetas(List<Etiqueta> listaEtiquetas) {
                this.listaEtiquetas = listaEtiquetas;
        }

}
