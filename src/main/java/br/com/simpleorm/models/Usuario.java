package br.com.simpleorm.models;

import br.com.simpleorm.annotations.Column;
import br.com.simpleorm.annotations.Id;
import br.com.simpleorm.annotations.Tabela;

@Tabela(value = "usuarios")
public class Usuario {
  @Id(value = "id")
  private Integer idUser;
  @Column(value = "nome")
  private String nome;
  @Column(value = "descricao")
  private String descricao;

  public Usuario() {

  }

  public Usuario(String nome, String descricao) {
    this.nome = nome;
    this.descricao = descricao;
  }

  @Override
  public String toString() {
    return "id: " + idUser + ", nome: " + nome + ", descricao: " + descricao;
  }

  public Integer getId() {
    return idUser;
  }
  public String getNome() {
    return nome;
  }
}
