package br.com.simpleorm;

import br.com.simpleorm.models.Usuario;
import br.com.simpleorm.sqlizen.BaseJdbc;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Optional;

public class Main {
  public static void main(String[] args)
      throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
    BaseJdbc<Usuario, Long> baseJdbc = new BaseJdbc<>(Usuario.class);
    Optional<Usuario> usuario = baseJdbc.findById(1L);


  }
}