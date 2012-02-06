package debug;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PeekDB {

    /**
     * For å se på innholdet i databasen.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	Class.forName("org.sqlite.JDBC");
	Connection conn = DriverManager.getConnection("jdbc:sqlite:avis.db");
	Statement stat = conn.createStatement();

	// ResultSet rs =
	// stat.executeQuery("select * from avisArtikler where url = 'http://www.vg.no/nyheter/utenriks/artikkel.php?artid=10077660';");
	ResultSet rs = stat.executeQuery("select * from avisArtikler;");
	int counter = 0;
	while (rs.next()) {
	    System.out.println("url = " + rs.getString("url"));
	    System.out.println("filepath = " + rs.getString("path"));
	    System.out.println();
	    counter++;
	}

	System.out.println("antall rader: " + counter);
	rs.close();
	conn.close();
	
	
	
    }
}