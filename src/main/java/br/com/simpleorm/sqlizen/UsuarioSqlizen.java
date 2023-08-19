package br.com.simpleorm.sqlizen;

import br.com.simpleorm.database.DatabaseConnection;
import br.com.simpleorm.annotations.Column;
import br.com.simpleorm.annotations.Id;
import br.com.simpleorm.annotations.Tabela;
import br.com.simpleorm.models.Usuario;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.postgresql.util.PSQLException;

public class UsuarioSqlizen {

  public static void main(String[] args) {
    var s = new UsuarioSqlizen();
    s.findById(1);
  }

  // TODO: Código com bug 24/07/23
  public Optional<Usuario> findById(Integer usuarioId) {

    if(Objects.isNull(usuarioId)) {
      System.out.println("Não é possível executar uma query sem ID do usuário");
      return Optional.empty();
    }

    String nomeTabela = Usuario.class.getAnnotation(Tabela.class).value();
    Connection conexao = DatabaseConnection.conectar();
    Statement statement = null;
    Optional<Usuario> usuario = Optional.empty();

    try {
      String idNomeColuna = null;
      Field[] fields = Usuario.class.getDeclaredFields();

      for(Field field : fields) {

        field.setAccessible(true);
        if(field.getAnnotation(Id.class) != null) {
          idNomeColuna = field.getAnnotation(Id.class).value();
        }
      }

      if(Objects.isNull(idNomeColuna)) {
        throw new RuntimeException("Entidade não tem ID Primary");
      }

      conexao.setReadOnly(true);
      statement = conexao.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM %s WHERE %s=%s", nomeTabela, idNomeColuna, usuarioId));
      boolean linhaValida = resultSet.next();
      if(linhaValida) {
        Integer id = resultSet.getInt(1);
        String nome = resultSet.getString(2);
        Constructor<?> cs = Usuario.class.getDeclaredConstructor(Object.class, Object.class);

        // TODO: resolver
        //usuario = Optional.of(Usuario.criar(id, nome));
      }
    }
    catch (PSQLException e) {
      e.printStackTrace();
    }
    catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Erro ao tentar procurar usuário por ID");
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } finally {
      try {
        statement.close();
        conexao.close();
      }
      catch (Exception e) {
        e.printStackTrace();
        System.err.println("Erro ao tentar fechar conexões");
      }
    }

    return usuario;
  }

  private List<Usuario> querySelect(String sql) {

    if(Objects.isNull(sql)) {
      throw new RuntimeException("Não é possível executar uma query nula");
    }

    Connection banco = DatabaseConnection.conectar();
    Statement statement = null;
    ResultSet resultSet = null;
    List<Usuario> usuarios = new ArrayList<>();

    try {
      statement = banco.createStatement();
      resultSet = statement.executeQuery(sql);
      Field[] fields = Usuario.class.getDeclaredFields();

      List<String> nomeColunas = new ArrayList<>();
      List<Type> types = new ArrayList<>();

      for (Field field : fields) {

        field.setAccessible(true);
        if(Objects.nonNull(field.getAnnotation(Id.class))) {
          types.add(field.getType());
          nomeColunas.add(field.getAnnotation(Id.class).value());
        }

        if(Objects.nonNull(field.getAnnotation(Column.class))) {
          types.add(field.getType());
          nomeColunas.add(field.getAnnotation(Column.class).value());
        }
      }

      Integer usuarioId = null;
      String usuarioNome = null;
      while(resultSet.next()) {

        for(int i = 0; i < nomeColunas.size(); i++) {

          if(types.get(i).getTypeName() == "java.lang.Integer") {
            usuarioId = resultSet.getInt(nomeColunas.get(i));
          }

          if(types.get(i).getTypeName() == "java.lang.String") {
            usuarioNome = resultSet.getObject(nomeColunas.get(i), String.class);
          }
        }
      }

      // TODO
      //usuarios.add(Usuario.criar(usuarioId, usuarioNome));
    }
    catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Erro ao tentar fazer consulta de usuários");
    }
    finally {

      try {
        resultSet.close();
        statement.close();
        banco.close();
      }
      catch (Exception e) {
        e.printStackTrace();
        System.err.println("Erro ao tentar desconectar serviços.");
      }
    }

    return usuarios;
  }


  public void update(Usuario usuario) {

    if(Objects.isNull(usuario)) {
      throw new RuntimeException("Não é possível atualizar usuário nulo");
    }

    if(Objects.isNull(usuario.getId())) {
      throw new RuntimeException("Esperasse que usuário tenha ID para poder atualizar");
    }

    Connection banco = DatabaseConnection.conectar();
    Statement statement = null;

    try {
      statement = banco.createStatement();
      String sql = queryUpdate(usuario);
      int alteracoes = statement.executeUpdate(sql);
      System.out.println("Usuário atualizado com sucesso");
    }
    catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Erro ao tentar atualizar usuário");
    }
    finally {

      try {
        statement.close();
        banco.close();
      }
      catch (Exception e) {
        e.printStackTrace();
        System.err.println("Erro ao tentar finalizar conexão com banco e statement");
      }
    }
  }
  public void salvar(Usuario usuario) {

    if(Objects.isNull(usuario)) {
      throw new RuntimeException("Não pode salvar um usuário nulo");
    }

    String sql = queryInsert(usuario);
    Connection connection = DatabaseConnection.conectar();
    Statement statement = null;

    try {
      connection.setAutoCommit(false);
      statement = connection.createStatement();
      int alteracoesFeitas = statement.executeUpdate(sql);

      if(alteracoesFeitas == 0) {
        System.err.println("Nenhuma alteração feita no banco de dados. Verifique SQL");
      }
      else if(alteracoesFeitas == 1) {
        System.out.println("Usuário salvo com sucesso");
      }

      connection.commit();
    }
    catch (PSQLException e) {
      e.printStackTrace();
      System.err.println("Erro ao tentar salvar usuário. Verifique se existe os campos no banco de dados.");
    }
    catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Erro ao tentar salvar usuário.");
    }
    finally {

      try {
        statement.close();
        connection.close();
      }
      catch (Exception e) {
        System.err.println("Erro ao tentar fechar Statement ou conexão com banco.");
        e.printStackTrace();
      }
    }
  }

  private String queryInsert(Usuario usuario) {

    if(Objects.isNull(usuario)) {
      throw new RuntimeException("Não pode criar query insert com usuario nulo.");
    }

    String nomeTabela = usuario.getClass().getAnnotation(Tabela.class).value();
    Field[] fields = usuario.getClass().getDeclaredFields();
    List<String> colunas = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for(Field field : fields) {
      field.setAccessible(true);
      if(Objects.isNull(field.getAnnotation(Column.class))) {
        continue;
      }

      Object valor = null;
      try {
        valor = field.get(usuario);
      }
      catch (Exception e) {
        System.err.println("Não foi possível obter campo.");
        e.printStackTrace();
      }

      String nomeColuna = field.getAnnotation(Column.class).value();
      colunas.add(nomeColuna);
      values.add(String.format("'%s'", valor.toString()));
    }

    String inserirColunas = String.join(",", colunas);
    String valuesColumn = String.join(",", values);
    String sql = String.format("INSERT INTO %s(%s) VALUES (%s)", nomeTabela, inserirColunas, valuesColumn);
    return sql;
  }
  private String queryUpdate(Usuario usuario) {

    if(Objects.isNull(usuario)) {
      throw new RuntimeException("Não é possível gerar query update com usuário nulo");
    }

    String nomeTabela = usuario.getClass().getAnnotation(Tabela.class).value();
    List<String> atributos = new ArrayList<>();
    Field[] fields = Usuario.class.getDeclaredFields();
    String idNomeColuna = "";
    String idUsuario = "";
    for (Field field : fields) {
      field.setAccessible(true);
      if(Objects.nonNull(field.getAnnotation(Id.class))) {
        idNomeColuna = field.getAnnotation(Id.class).value();
        try {
          idUsuario = field.get(usuario).toString();
        }
        catch (Exception e) {
          System.err.println("Não foi possível obter ID do usuário para gerar query update");
        }
      }

      if(Objects.isNull(field.getAnnotation(Column.class))) {
        continue;
      }

      Object atributo = null;
      try {
        atributo = field.get(usuario);
      }
      catch (Exception e) {
        System.err.println("Não foi possível obter variável do usuário ao tentar atualizar dados");
      }

      String coluna = field.getAnnotation(Column.class).value();
      atributos.add(String.format("SET %s=%s", coluna, String.format("'%s'", atributo.toString())));
    }

    String setUpdate = String.join(" ", atributos);
    String sql = String.format("UPDATE %s %s WHERE %s=%s", nomeTabela, setUpdate, idNomeColuna, idUsuario);
    return sql;
  }
}
