package afinidadPersonas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectionSingleton {
	private static Connection con;

	public static Connection getConnection() throws SQLException {
		String url = "jdbc:postgresql://192.168.1.130:5432/afinidad";
		String user = "alumno";
		String password = "alumno";
		if (con == null || con.isClosed()) {
			con = DriverManager.getConnection(url, user, password);
		}
		return con;
	}


}

