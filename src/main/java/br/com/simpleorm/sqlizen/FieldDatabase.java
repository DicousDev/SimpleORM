package br.com.simpleorm.sqlizen;

import java.lang.reflect.Field;

public class FieldDatabase<T> {

  public Field field;
  public String columnName;

  public FieldDatabase(Field field, String columnName) {
    this.field = field;
    this.columnName = columnName;
  }

  public Object getFieldValue(T data) {

    try {
      field.setAccessible(true);
      return field.get(data);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException("Erro ao tentar acessar Field Value: " + field);
    }
  }
}
