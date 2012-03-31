package snt;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SntTask extends TimerTask {
	private static String fileseparator = System.getProperty("file.separator");
	static DateFormat dateFormat = new SimpleDateFormat("HH.mm.ss_dd-MM-yyyy");
	private static Date date;
	private static String dateNow;
	private static String dateDay;
	private static String newHash;
	private static Elements articleURLs;

	public SntTask() {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://vg.no").get();
			Elements artiklerTilHash = doc.select(".article-content");

			// mekk artikler uten dynamiske linker til bilder og saa hash
			// greiene
			for (Element tikkel : artiklerTilHash) {
				Elements imgTags = tikkel
						.select(".df-img-container-inner a img");
				for (Element e : imgTags) {
					e.attr("src", e.attr("src").substring(9)); // fjern variable
																// data i url
				}

			}
			newHash = Snt.MD5(artiklerTilHash);

			// hvis hashes ikke er like, ja, da er det en forandring
			if (!Snt.latestHash.equals(newHash)) {
				updateDate();
				System.out.println(dateNow + "  " + newHash);
				Snt.latestHash = newHash;

				filepath = dateDay + fileseparator + "VG-" + dateNow
						+ ".html";
				doc = Jsoup.connect("http://vg.no").get();
				Elements artikler = doc.select(".article-content");
				articleURLs = artikler.select("a[href]");
				initDB();
				insertArticleInDB();
				getImages(doc);
				Document docRelativeLinker = gjorForsidenRelativ(doc);
				WriteToFileLineByLine(dateDay + fileseparator + "VG-" + dateNow
						+ ".html", docRelativeLinker.html());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private static Document gjorForsidenRelativ(Document doc) {

		// lagre bilder
		Elements links = doc.select("img[src]");
		for (Element e : links) {
			String oldAddress = e.attr("src");
			String bildeNavn = oldAddress
					.substring(oldAddress.lastIndexOf("/") + 1);
			String newAddress = "images/" + bildeNavn;

			e.attr("src", newAddress);
		}

		// vi trenger bare laste ned hvis den ikke allerede finnes
		if (makeFolderIfNotExists(dateDay + fileseparator + "css")) {
			lagreCSS(doc);
		}

		// skifte ut csslink til den paa disk
		Elements csss = doc.select("link[type=text/css]");
		
		// TODO hent ned alle css filer og putt de i samme fil :D 
		for (Element css : csss) {
			css.attr("href", "css/"+ dateDay + ".css");
		}

		// fjern <noscript>
		// TODO det boer vaere en bedre maate aa gjoere dette paa
		String ns1 = doc.html().replace("<noscript>", "");
		String ns2 = ns1.replace("</noscript>", "");

		return Jsoup.parse(ns2);
	}

	private static List<String> getCSSlink(Document doc) {
		List<String> csss = new LinkedList<String>();
		Elements stylesheets = doc.select("link[type=text/css]");

		for (Element css : stylesheets) {
			String href = css.attr("href");
			if (isValid(href)) {
				csss.add(href);
			} else {
				csss.add(doc.baseUri() + href);
			}
		}
		return csss;
	}

	private static boolean isValid(String url) {
		return url.startsWith("http") || url.startsWith("www");
	}

	private static void lagreCSS(Document doc) {
		// last ned css til /css/
		List<String> cssURl = getCSSlink(doc);
		StringBuilder cssContent = new StringBuilder();
		for (String css : cssURl) {
			cssContent.append(getURLContent(css));
		}
		
		try {
			WriteToFileLineByLine(dateDay + fileseparator + "css"
					+ fileseparator + dateDay + ".css", cssContent.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void WriteToFileLineByLine(String pathToNewFile, String output)
			throws IOException {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					pathToNewFile));
			out.write(output);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getURLContent(String s) {
		URL url = null;
		String content = "";
		try {
			url = new URL(s);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				content += line;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private static BufferedImage saveImages(String img) {
		URLConnection connection = null;
		InputStream ins = null;
		try {
			connection = new URL(img).openConnection();
			connection.connect();
			ins = connection.getInputStream();
			return ImageIO.read(ins);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void getImages(Document selection) throws IOException {
		makeFolderIfNotExists(dateDay);
		makeFolderIfNotExists(dateDay + fileseparator + "images");

		FileOutputStream fos = null;
		Elements artikkelBilderTags = selection.select("img[src]");
		for (Element img : artikkelBilderTags) {
			String src = img.attr("src");
			
			
			if (src.contains("?")) {
				src = src.substring(0,src.lastIndexOf("?"));
			}
			if (src.startsWith("//")){
				src = "http:" + src;
			}else if(src.startsWith("/")){
				src = selection.baseUri() + src;
			} else if(!isValid(src)){
				src = selection.baseUri() + src;
			}
			
				
			String bildeNavn = src.substring(src.lastIndexOf("/") + 1);
			String bildeSti = dateDay + fileseparator
			+ "images" + fileseparator + bildeNavn;
			if (!fileExists(bildeSti)) {
			fos = new FileOutputStream(new File(bildeSti));
				ImageIO.write((RenderedImage) saveImages(src),
						bildeNavn.substring(bildeNavn.length() - 3), fos);
			}

		}
	}

	private static boolean fileExists(String bildeSti) {
	    File f = new File(bildeSti);
	    return f.exists();
	}

	/**
	 * @param folderName
	 * @return true if folder has been created
	 */
	private static boolean makeFolderIfNotExists(String folderName) {
		File f = new File(folderName);
		if (!f.exists()) {
			f.mkdir();
			System.out.println("Opprettet mappe " + f.toString());
			return true;
		}
		return false;
	}

	private static void updateDate() {
		date = new Date(System.currentTimeMillis());
		dateNow = dateFormat.format(date);
		dateDay = new SimpleDateFormat("dd-MM-yyyy").format(date);
	}

	@Override
	public void run() {
		new SntTask();
	}

	private static String filepath;
	private static Connection conn;

	private static boolean exists(String url, String path) {
		Statement stat;
		try {
			stat = conn.createStatement();
			ResultSet rs = stat
					.executeQuery("SELECT count(*) FROM avisArtikler WHERE url ='"
							+ url + "' and path='" + path + "'");
			if (rs.getInt(1) > 0){
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private void initDB() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:avis.db");
			if (conn != null) {
				Statement stat = conn.createStatement();
				stat.executeUpdate("create table if not exists avisArtikler (url, path);");
			} else {
				updateDate();
				System.err.println("Did not initialize JDBC connection at "
						+ dateNow);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void insertArticleInDB() {
		if (conn != null) {
			try {			
				for (Element url : articleURLs) {
					String rawUrl = url.attr("href");
				
					if (isValid(rawUrl) && !exists(rawUrl, filepath)) { // vil ikke
																	// ha
						// duplikater
						// vil vi
						// vel.
					Statement statement = conn.createStatement();
					statement.execute("insert into avisArtikler values('"+rawUrl +"','"+filepath + "');");

					}
				}
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
