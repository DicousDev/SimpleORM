package br.com.simpleorm.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "postagem")
public class Postagem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "titulo")
  private String titulo;
  @Column(name = "nome_autor")
  private String nomeAutor;

  public Postagem() {

  }

  public Postagem(Long id, String titulo, String nomeAutor) {
    this.id = id;
    this.titulo = titulo;
    this.nomeAutor = nomeAutor;
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder("id: ");
    string.append(id);
    string.append(", titulo: ");
    string.append(titulo);
    string.append(", nomeAutor: ");
    string.append(nomeAutor);
    return string.toString();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getNomeAutor() {
    return nomeAutor;
  }

  public void setNomeAutor(String nomeAutor) {
    this.nomeAutor = nomeAutor;
  }
}
