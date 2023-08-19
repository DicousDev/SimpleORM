package br.com.simpleorm.repositories;

import br.com.simpleorm.models.Usuario;
import br.com.simpleorm.sqlizen.UsuarioSqlizen;
import java.util.Optional;

public class UsuarioRepository {

  private UsuarioSqlizen sqlizen;

  public UsuarioRepository() {
    sqlizen = new UsuarioSqlizen();
  }

  public Optional<Usuario> getUsuario(Integer usuarioId) {
    return sqlizen.findById(usuarioId);
  }
}
