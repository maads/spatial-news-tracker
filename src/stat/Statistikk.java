package stat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Statistikk {

	private static Connection conn;

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initDB();
		
		String avis = args[0];
		String dato = args[1];
		
//		String avis = "NRK";
//		String dato = "13-04-2012";
		
		if (conn != null) {

			int[] oversikt = new int[24];
			StringBuilder nummerTilGraf = new StringBuilder();

			Statement stat;
			try {
				for (int i = 0; i <= 23; i++) {
						stat = conn.createStatement();
						ResultSet rs = stat
								.executeQuery("select distinct path from avisArtikler where path LIKE '"
										+ dato + "/" + avis + "-" + modI(i)	+ "%'");
						int counter = 0;
						while (rs.next()) {
							counter++;
						}
						oversikt[i] = counter;
						nummerTilGraf.append(" " + counter);
				}
				int total = 0;
				for (int i = 0; i <= 23; i++) {
						System.out.println("Endringer kl " + modI(i) + ": "	+ oversikt[i]);
						total += oversikt[i];
				}
				System.out.println("Endringer totalt den " + dato + ": " + total);
//				System.out.println(nummerTilGraf.toString());

			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			System.err.println("Did not initialize JDBC connection");
		}
	}

	private static String modI(int i) {
		if (i < 10)
			return "0" + i;
		return String.valueOf(i);
	}

	private static void initDB() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:avis.db");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
