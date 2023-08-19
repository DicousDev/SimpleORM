package br.com.simpleorm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

  public final static String DATABASE_DB = "todo";
  public final static String DATABASE_USERNAME = "todo";
  public final static String DATABASE_PASSWORD = "todo";
  public final static String DATABASE_PORT = "5432";
  public final static String DATABASE_HOST = "localhost";
  public final static String DATABASE_URL = String.format("jdbc:postgresql://%s:%s/%s", DATABASE_HOST, DATABASE_PORT, DATABASE_DB);

  public static Connection conectar() {
    try {
      Properties props = new Properties();
      props.setProperty("user", DATABASE_USERNAME);
      props.setProperty("password", DATABASE_PASSWORD);
      props.setProperty("TimeZone", "America/SaoPaulo");
      return DriverManager.getConnection(DATABASE_URL, props);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Erro ao tentar conectar com banco de dados. \n "
          + "Verifique se username, password, url estão corretos e verifique se está executando banco de dados no Docker.");
    }
  }
}
