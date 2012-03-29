package sok;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Mads,
 * 
 */
@SuppressWarnings("serial")
public class Search extends JFrame {
	private JTextField urlInputField;
	private JButton searchBtn;
	static Connection conn;
	private JCheckBox exportCheckbox;

	private String fileSeperator = System.getProperty("file.separator");

	public Search() {
		initGUI();
	}

	private void initGUI() {
		urlInputField = new JTextField("Lim inn artikkel-URL");
		setLayout(new GridLayout(0, 1));
		searchBtn = new JButton("Generer 'tidslinje' basert på artikkel");
		exportCheckbox = new JCheckBox("Kopier filer fra opprinnelig plassering til ny mappe");
		this.add(urlInputField);
		this.add(exportCheckbox);
		this.add(searchBtn);
		searchBtn.addActionListener(new ActionListener() {// egen classe for
					// actionListner

					public void actionPerformed(ActionEvent arg0) {
						initDB();
						int counter = 0;
						String pathToFolder = System.getProperty("user.dir")
								+ fileSeperator + "output";
						Statement stat;
						try {
							makeDirs();
							stat = conn.createStatement();
							ResultSet rs = stat
									.executeQuery("select distinct path from avisArtikler where url = '"
											+ getInputField() + "';");
							while (rs.next()) {
								counter++;
								String path = rs.getString("path");
								path = path.replace("\\", fileSeperator);
								Document doc = Jsoup.parse(new File(path),
										"UTF-8");
								fixCSSandImages(doc, exportCheckbox.isSelected());
								WriteToFile(
										"output"
												+ fileSeperator
												+ path.substring(path
														.indexOf(fileSeperator)),
										markArticleinFile(getInputField(), doc));
							}
							String beskjed = "Fant artikkelen på " + counter + " forsider.";
							if(counter > 0)
							    beskjed +=  "\nKikk i " + pathToFolder;
							JOptionPane.showMessageDialog(null,
									beskjed);
						} catch (SQLException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null,
									"Noe gikk galt. \n" + e.getMessage());
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null,
									"Noe gikk galt. \n" + e.getMessage());
							e.printStackTrace();
						} finally {
							closeWindow();
						}

					}

				});
		this.setSize(700, 150);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void makeDirs() {
		File f = new File("output");
		if (!f.exists()) {
			f.mkdir();
		}
		if (exportCheckbox.isSelected()) {
		    f = new File("output" + fileSeperator + "css");
		    if (!f.exists()) {
			f.mkdir();
		    }
		    f = new File("output" + fileSeperator + "images");
		    if (!f.exists()) {
			f.mkdir();
		    }
		}
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

	private String markArticleinFile(String url, Document document)
			throws IOException {
		Elements articles = document.select(".article-content");
		for (Element article : articles) {
			Elements articleLinks = article.select("a[href]");
			for (int i = 0; i < articleLinks.size(); i++) {
				if (articleLinks.get(i).attr("href").equals(url)) {
					System.out.println(url);
					article.attr("style", "background-color:yellow;");
					break;
				}
			}
		}
		return document.html();
	}

	private Document fixCSSandImages(Document doc, boolean export)
			throws IOException {

		String tempDato = doc.baseUri().substring(0,
				doc.baseUri().lastIndexOf(fileSeperator));
		String dato = tempDato.substring(
				tempDato.lastIndexOf(fileSeperator) + 1, tempDato.length());
		System.out.println(dato);
		Elements csss = doc.select("link[type=text/css]");
		if (!export) {
			for (Element cs : csss) {
				cs.attr("href", "../" + dato + "/css/" + dato + ".css");
			}

			Elements imgs = doc.select("img[src]");
			for (Element img : imgs) {
				String prevLink = img.attr("src");
				img.attr("src", "../" + dato + "/"
						+ prevLink);
			}
		} else {
			String output = System.getProperty("user.dir") + fileSeperator
					+ "output" + fileSeperator;
			String[] dirs = { "/images/" , "/css/" };
			for (String s : dirs) {
				File startDir = new File(System.getProperty("user.dir")
						+ fileSeperator + dato + s);
				File[] files = startDir.listFiles();
				for (File file : files) {
					copy(file, new File(output + s + file.getName()));
				}
			}

		}
		return doc;
	}

	private void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
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