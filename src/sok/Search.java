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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.SNTUtil;

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

	static String searchURL;
	static boolean exportOption;

	public Search(boolean showGUI) {
		if (showGUI) {
			initGUI();
		} else {
			searchDatabase(showGUI);
		}
	}

	private void initGUI() {
		urlInputField = new JTextField("Lim inn artikkel-URL");
		setLayout(new GridLayout(0, 1));
		searchBtn = new JButton("Generer 'tidslinje' basert på artikkel");
		exportCheckbox = new JCheckBox(
				"Kopier filer fra opprinnelig plassering til ny mappe");
		this.add(urlInputField);
		this.add(exportCheckbox);
		this.add(searchBtn);
		searchBtn.addActionListener(new ActionListener() {// egen classe for
					// actionListner

					public void actionPerformed(ActionEvent arg0) {
						exportOption = exportCheckbox.isSelected();
						searchURL = getInputField();
						searchDatabase(true);

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
		if (exportOption) {
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
		Elements csss = doc.select("link[type=text/css]");
		if (!export) {
			for (Element cs : csss) {
				cs.attr("href", "../" + dato + "/css/" + dato + ".css");
			}

			Elements imgs = doc.select("img[src]");
			for (Element img : imgs) {
				String prevLink = img.attr("src");
				img.attr("src", "../" + dato + "/" + prevLink);
			}
		} else {
			String output = System.getProperty("user.dir") + fileSeperator
					+ "output" + fileSeperator;
			String[] dirs = { "/images/", "/css/" };
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

	private void searchDatabase(boolean guiVisible) {
		initDB();
		int counter = 0;
		String pathToFolder = System.getProperty("user.dir") + fileSeperator
				+ "output";
		Statement stat;
		try {
			makeDirs();
			stat = conn.createStatement();
			ResultSet rs = stat
					.executeQuery("select distinct path from avisArtikler where url = '"
							+ searchURL + "';");
			while (rs.next()) {
				counter++;
				String path = rs.getString("path");
				path = path.replace("\\", fileSeperator);
				Document doc = Jsoup.parse(new File(path), "UTF-8");
				fixCSSandImages(doc, exportOption);
				SNTUtil.WriteToFileLineByLine(
						"output" + fileSeperator
								+ path.substring(path.indexOf(fileSeperator)),
						markArticleinFile(searchURL, doc));
			}
			String beskjed = "Fant artikkelen på " + counter + " forsider.";
			if (counter > 0)
				beskjed += "\nKikk i " + pathToFolder;
			if (guiVisible) {
				JOptionPane.showMessageDialog(null, beskjed);
			} else {
				System.out.println(beskjed);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			if (guiVisible) {
				JOptionPane.showMessageDialog(null,
						"Noe gikk galt. \n" + e.getMessage());
			} else {
				System.out.println("Noe gikk galt. \n" + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			if (guiVisible) {
				JOptionPane.showMessageDialog(null,
						"Noe gikk galt. \n" + e.getMessage());
			} else {
				System.out.println("Noe gikk galt. \n");
				e.printStackTrace();
			}
		} finally {
			closeWindow();
		}
	}

//	public static void WriteToFile(String pathToNewFile, String output) {
//		try {
//			BufferedWriter out = new BufferedWriter(new FileWriter(
//					pathToNewFile));
//			out.write(output);
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public static void main(String[] args) {
		boolean showGUI = true;
		if (args.length > 0) {
			searchURL = args[0];
			if (args.length > 1) {
				exportOption = args[1].equals("export");
			}
			System.out.println("Artikkelen du søker: " + searchURL);
			showGUI = false;
		}
		new Search(showGUI);
	}
}