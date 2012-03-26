package sok;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
 * @author Mads, 
 * 
 */
@SuppressWarnings("serial")
public class Search extends JFrame {
	private JTextField urlInputField;
	private JButton searchBtn;
	static Connection conn;

	private String fileSeperator = System.getProperty("file.separator");

	public Search() {
			initGUI();
	}

	private void initGUI() {
		urlInputField = new JTextField("Lim inn artikkel-URL");
		setLayout(new GridLayout(0, 1));
		searchBtn = new JButton("Generer 'tidslinje' basert på artikkel");
		this.add(urlInputField);
		this.add(searchBtn);
		searchBtn.addActionListener(new ActionListener() {// egen classe for actionListner

					public void actionPerformed(ActionEvent arg0) {
						initDB();
						int counter = 0;
						String pathToFolder = System.getProperty("user.dir")
								+ fileSeperator + "output";
						Statement stat;
						try {
							File f = new File("output");
							if (!f.exists()) {
								f.mkdir();
							}
							stat = conn.createStatement();
							ResultSet rs = stat
									.executeQuery("select distinct path from avisArtikler where url = '" + getInputField() + "';");

							while (rs.next()) {
								counter++;
								String path = rs.getString("path");
								path = path.replace("\\", fileSeperator);

								writeAndMarkArticleInFile(
										getInputField(),
										path,
										"output"
												+ fileSeperator
												+ path.substring(path
														.indexOf(fileSeperator)));
							}
							JOptionPane.showMessageDialog(null,
									"Fant artikkelen på " + counter
											+ " forsider. Kikk i " + pathToFolder);
						} catch (SQLException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null,
									"Noe gikk galt. \n" + e.getMessage());
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null,
									"Noe gikk galt. \n" + e.getMessage());
						} finally {
							closeWindow();
						}
						
				
					}

				});
		this.setSize(700, 150);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	protected void closeWindow() {
		this.dispose();
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

	private void writeAndMarkArticleInFile(String url, String inputPath,
			String outputPath) {
		WriteToFile(outputPath, markArticleinFile(url, inputPath));

	}

	private String markArticleinFile(String url, String inputPath) {
		BufferedReader in;
		String inputLine;

		// ta dokumentet. skift url til css med .../dato/css/, samme med bilder...
		
		String startDiv = "<div class=\"article-content\">";
		String endDiv = "</div>";
		boolean inArticle = false;
		StringBuilder page = new StringBuilder();

		StringBuilder tempLines = new StringBuilder();
		try {
			in = new BufferedReader(new FileReader(inputPath));
			while ((inputLine = in.readLine()) != null) {
				if (inArticle) {
					tempLines.append("\n" + inputLine);
					if (inputLine.equals(endDiv)) {
						page.append(markArticle(tempLines.toString(), url));
						tempLines = new StringBuilder();
						inArticle = false;
					}
				} else {
					if (inputLine.trim().equals(startDiv)) {
						tempLines.append("\n" + inputLine);
						inArticle = true;
					} else {
						page.append("\n" + inputLine);
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return page.toString();
	}

	private String markArticle(String article, String url) {
		//TODO fix this.
		return article;
	}

	public static void WriteToFile(String pathToNewFile, String output) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					pathToNewFile));
			out.write(output);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Search();
	}
}