package dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonConnection {

    private static Connection connection;
    // charger le driver
    private SingletonConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/projet_reconnaissance",
                    "root",
                    "root"
            );

            System.out.println("Connexion etablie");

        } catch (ClassNotFoundException e) {
            System.out.println("Driver introuvable");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erreur de connexion a la base");
            e.printStackTrace();
        }
    }
    // donner acces a la connexion unique
    public static Connection getConnection() {
        if (connection == null) {
            new SingletonConnection();
        }
        return connection;
    }
}