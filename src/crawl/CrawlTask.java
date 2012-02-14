package crawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimerTask;

public class CrawlTask extends TimerTask {

	public static ArrayList<ArrayList<String>> allArticle = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> allArticleOld = new ArrayList<ArrayList<String>>();
	public static ArrayList<String> articleURLs = new ArrayList<String>();
	public static ArrayList<String> article = new ArrayList<String>();

	static DateFormat dateFormat = new SimpleDateFormat("HH.mm.ss_dd-MM-yyyy");
	static Date date;
	static String dateNow;
	static String dateDay;

	static String fileSeperator = System.getProperty("file.separator");
	static String filepath;

	static Connection conn;

	@Override
	public void run() {
		try {
			initDB();
			String vg = crawl("http://www.vg.no/", "vg");

			if (!allArticleOld.equals(allArticle)) {

				allArticleOld.clear();
				allArticleOld.addAll(allArticle);
				allArticle.clear();

				updateDate();
				AvisCrawl.setLabelText(dateNow); // stygg hack.. :/
				createArticleFolder(dateDay);
				String file = dateDay + fileSeperator + "VG-" + dateNow
						+ ".html";
				setPath(file);
				Util.WriteToFileLineByLine(filepath, dateNow + vg);
				insertArticleInDB();
			}
			allArticle.clear();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void setPath(String path) {
		filepath = path;
	}

	static void insertArticleInDB() {
		if (conn != null) {
			try {
				PreparedStatement prep = conn
						.prepareStatement("insert into avisArtikler values(?,?);");

				for (String url : articleURLs) {
					if (isValid(url) && !exists(url, filepath)) { // vil ikke ha
																	// duplikater
																	// vil vi
																	// vel.
						prep.setString(1, url);
						prep.setString(2, filepath);
						prep.addBatch();
					}
				}
				prep.executeBatch();
				conn.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean isValid(String url) {
		return url.startsWith("http") || url.startsWith("mailto")
				|| url.startsWith("www");
	}

	private static boolean exists(String url, String path) {
		Statement stat;
		try {
			stat = conn.createStatement();
			ResultSet rs = stat
					.executeQuery("select count(*) from avisArtikler where url = '"
							+ url + "' and path='" + path + "';");
			if (rs.getInt(1) > 0) {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static void createArticleFolder(String folderName) {
		File f = new File(folderName);
		if (!f.exists()) {
			f.mkdir();

			f = new File(folderName + fileSeperator + "css");
			f.mkdir();

			Util.generateCSSFolder(folderName);

		}
	}

	private String crawl(String urlPath, String avis) throws IOException {
		URL url = new URL(urlPath);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		String inputLine;
		String page = "";
		if (avis.equals("vg")) {
			String startDiv = "<div class=\"article-content\">";
			String endDiv = "</div>";
			boolean inArticle = false;
			while ((inputLine = in.readLine()) != null) {
				page += "\n" + inputLine;
				if (inArticle) {
					if (inputLine.trim().equals(endDiv)) {
						allArticle.add(article);

						articleURLs.add(fetchURL(article));

						inArticle = false;
						article = new ArrayList<String>();
						continue;

					} else if (inputLine.indexOf("static") != -1) {
						int indexOfStatic = inputLine.indexOf("static");
						inputLine = inputLine.replace(inputLine.substring(
								indexOfStatic, indexOfStatic + 9), "");
					}
					article.add(inputLine);

				} else {
					if (inputLine.trim().equals(startDiv)) {
						inArticle = true;
						article.add(inputLine);
					}
				}

			}
			in.close();

			return page;
		} else {
			return null;
		}
	}

	private String fetchURL(ArrayList<String> arti) {

		String aHrefStart = "<a href=\"";
		String span = "</span></span></span>";

		String artiString = arti.toString();
		artiString = artiString.substring(artiString.indexOf(span)
				+ span.length());
		artiString = artiString.substring(artiString.indexOf(aHrefStart)
				+ aHrefStart.length());
		String link = artiString.substring(0, artiString.indexOf("\""));

		return link;
	}

	private static void updateDate() {
		date = new Date(System.currentTimeMillis());
		dateNow = dateFormat.format(date);
		dateDay = new SimpleDateFormat("dd-MM-yyyy").format(date);
	}

	private void initDB() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:avis.db");
			if (conn != null) {
				Statement stat = conn.createStatement();
				stat.executeUpdate("create table if not exists avisArtikler (url, path);");
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
