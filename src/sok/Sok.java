/**
 * 
 */
package sok;

import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @author Mads
 * 
 */
@SuppressWarnings("serial")
public class Sok extends JFrame {

    private JTextField urlInputField;
    private JButton searchBtn;
    static Connection conn;

    public Sok() {
	initGUI();
    }

    private void initGUI() {
	urlInputField = new JTextField("Lim inn artikkel-URL");
	setLayout(new GridLayout(0, 1));
	searchBtn = new JButton("Generer 'tidslinje' basert på artikkel");
	this.add(urlInputField);
	this.add(searchBtn);
	searchBtn.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		initDB();
		int counter = 0;
		String pathToFolder = System.getProperty("user.dir")+System.getProperty("file.separator");
		Statement stat;
		try {
		    stat = conn.createStatement();
		    ResultSet rs = stat
			    .executeQuery("select distinct path from avisArtikler where url = '"
				    + getInputField() + "';");

		    while (rs.next()) {
			System.out.println("filepath = " + rs.getString("path"));
			counter++;
		    }
		    // generer filer med rett markering på gitt artikkel. utvid pathToFolder med den nye mappen
		    
		    System.out.println("antall rader: " + counter);
		    int yesNoOption = JOptionPane
			    .showConfirmDialog(
				    null,
				    "Fant artikkelen på "
					    + counter
					    + " forsider. Har generert disse sidene for visning i mappe: "
					    + pathToFolder
					    + ". Åpne denne mappen nå?",
				    "tl;dr; åpne mappe?",
				    JOptionPane.YES_NO_OPTION);

		    // yes 0, no 1.
		    if (yesNoOption == JOptionPane.YES_OPTION) {
			try {
			    Desktop.getDesktop().open(new File(pathToFolder));
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }

		} catch (SQLException e) {
		    e.printStackTrace();
		}

	    }
	});
	this.setSize(700, 150);
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setVisible(true);
    }

    public String getInputField() {
	return urlInputField.getText();
    }

    private void initDB() {
	try {
	    Class.forName("org.sqlite.JDBC");
	    conn = DriverManager.getConnection("jdbc:sqlite:avis.db");
	    Statement stat = conn.createStatement();
	    stat.executeUpdate("create table if not exists avisArtikler (url, path);");

	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	new Sok();
    }
}
