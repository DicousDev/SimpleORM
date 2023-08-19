package br.com.simpleorm.sqlizen;

import br.com.simpleorm.annotations.Column;
import br.com.simpleorm.annotations.Id;
import br.com.simpleorm.annotations.Tabela;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableDatabase<T> {

  private Constructor<T> construtorEmpty;
  private String nomeTabela;
  private Map<String, FieldDatabase> fields = new HashMap<>();
  private Class<T> clazz;
  private FieldDatabase primaryKeyField = null;

  public TableDatabase(Class<T> clazz) {
    this.clazz = clazz;

    try {
      this.construtorEmpty = clazz.getConstructor();
    }
    catch (NoSuchMethodException e) {
      System.err.println(e.getLocalizedMessage());
      System.err.println("Erro ao tentar procurar construtor sem parâmetros, verifique se a class possui um construtor PUBLIC vazio.");
    }

    this.nomeTabela = clazz.getAnnotation(Tabela.class).value();
    Field[] fields = clazz.getDeclaredFields();
    for(Field field : fields) {
      FieldDatabase fieldDatabase = null;
      if(field.getAnnotation(Id.class) != null) {
        String nomeColuna = field.getAnnotation(Id.class).value();
        fieldDatabase = new FieldDatabase(field, nomeColuna);
        primaryKeyField = fieldDatabase;
      }
      else if(field.getAnnotation(Column.class) != null) {
        String nomeColuna = field.getAnnotation(Column.class).value();
        fieldDatabase = new FieldDatabase(field, nomeColuna);
      }
      else {
        System.out.println(String.format("Coluna não identificada. Field: %s", field.getName()));
      }

      if(fieldDatabase != null) {
        this.fields.put(fieldDatabase.columnName, fieldDatabase);
      }
    }
  }

  public List<String> getColumnsName() {
    return fields.values().stream()
        .map(field -> field.columnName)
        .collect(Collectors.toList());
  }

  public Field getFieldByColumnName(String columnName) {
    return fields.get(columnName).field;
  }

  public Object getFieldValueByColumnName(String columnName, T data) {
    return fields.get(columnName).getFieldValue(data);
  }

  public Object createInstance()
      throws InvocationTargetException, InstantiationException, IllegalAccessException {
    return construtorEmpty.newInstance();
  }

  public boolean existsColumnName(String column) {
    return fields.containsKey(column);
  }

  public String getTableName() {
    return nomeTabela;
  }

  public String getColumnNamePrimaryKey() {
    return primaryKeyField.columnName;
  }
}
