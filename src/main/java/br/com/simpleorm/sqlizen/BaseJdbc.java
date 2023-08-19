package br.com.simpleorm.sqlizen;

import br.com.simpleorm.database.DatabaseConnection;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BaseJdbc<T, ID> {

  private TableDatabase<T> table;

  public BaseJdbc(Class<T> clazz) {
    this.table = new TableDatabase<T>(clazz);
  }

  public Optional<T> findById(ID id)
      throws InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
    if(Objects.isNull(table)) {
      throw new RuntimeException("Não é possível fazer consultas com table null");
    }

    Connection connection = DatabaseConnection.conectar();
    connection.setReadOnly(true);
    String tableName = table.getTableName();
    String query = String.format("SELECT * FROM %s WHERE %s=%s", tableName, table.getColumnNamePrimaryKey(), id);
    PreparedStatement preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    ResultSet resultSet = preparedStatement.executeQuery();

    Optional<T> result = Optional.empty();
    if(resultSet.next()) {
      ResultSetMetaData metaData = resultSet.getMetaData();
      Object instance = table.createInstance();
      for(int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
        String columnName = metaData.getColumnName(columnIndex);
        if(table.existsColumnName(columnName)) {
          Object value = resultSet.getObject(columnIndex);
          Field field = table.getFieldByColumnName(columnName);
          field.setAccessible(true);
          field.set(instance, value);
        }
        else {
          System.out.println(String.format("Coluna não mapeada no código: %s", columnName));
        }
      }

      result = Optional.of((T) instance);
    }

    connection.close();
    preparedStatement.close();
    return result;
  }

  public void save(T data) throws SQLException {
    Connection connection = DatabaseConnection.conectar();
    String prepareSql = insertQuery();
    PreparedStatement preparedStatement = connection.prepareStatement(prepareSql);

    List<String> parametros = getQueryInsertParameters();
    for(int indexParameter = 0; indexParameter < parametros.size(); indexParameter++) {
      String columnName = parametros.get(indexParameter);
      preparedStatement.setObject(indexParameter + 1, table.getFieldValueByColumnName(columnName, data));
    }

    int linhasAlteradas = preparedStatement.executeUpdate();
    preparedStatement.close();
    connection.close();
  }

  public void update(ID id, T data) throws SQLException {
    Connection connection = DatabaseConnection.conectar();
    String prepareQuery = updateQuery();
    PreparedStatement preparedStatement = connection.prepareStatement(prepareQuery);

    List<String> parametros = getQueryUpdateParameters();
    for(int i = 0; i < parametros.size(); i++) {
      String columnName = parametros.get(i);
      preparedStatement.setObject(i + 1, table.getFieldValueByColumnName(columnName, data));
    }

    preparedStatement.setObject(parametros.size() + 1, id);

    int linhasAlteradas = preparedStatement.executeUpdate();
    preparedStatement.close();
    connection.close();
  }

  private String updateQuery() {
    StringBuilder query = new StringBuilder(String.format("UPDATE %s ", table.getTableName()));
    List<String> columns = getQueryUpdateParameters();
    boolean first = true;
    for(int i = 0; i < columns.size(); i++) {

      if(first) {
        first = false;
        query.append(String.format("set %s=? ", columns.get(i)));
      }
      else {
        query.append(String.format(", %s=?", columns.get(i)));
      }
    }

    query.append(String.format(" WHERE %s=?", table.getColumnNamePrimaryKey()));
    return query.toString();
  }


  private String insertQuery() {
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    sql.append(table.getTableName());
    sql.append("(");

    List<String> columnsNames = getQueryInsertParameters();
    sql.append(String.join(",", columnsNames));
    sql.append(") VALUES (");

    for(int i = 0; i < columnsNames.size() - 1; i++) {
      sql.append("?,");
    }

    sql.append("?)");
    return sql.toString();
  }

  private List<String> getQueryInsertParameters() {
    List<String> columnsNames = table.getColumnsName();
    columnsNames.remove(table.getColumnNamePrimaryKey());
    return columnsNames;
  }

  private List<String> getQueryUpdateParameters() {
    List<String> columnsNames = table.getColumnsName();
    columnsNames.remove(table.getColumnNamePrimaryKey());
    return columnsNames;
  }
}
