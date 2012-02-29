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



	static DateFormat dateFormat = new SimpleDateFormat("HH.mm.ss_dd-MM-yyyy");
	static Date date;
	static String dateNow;
	static String dateDay;

	static String fileSeperator = System.getProperty("file.separator");
	static String filepath;

	
	static Connection conn;
	private ArrayList<String> article = new ArrayList<String>();
	private static ArrayList<String> articleURLs = new ArrayList<String>();

	@Override
	public void run() {
		updateDate();
		
		AvisCrawl.taskCounter++;
		System.out.print(AvisCrawl.taskCounter + "("+ dateNow.substring(0, 8)+"),");
		if(AvisCrawl.taskCounter % 40 == 0)
			System.out.println("\n" + dateNow + " | Number of tasks started in this run so far: " + AvisCrawl.taskCounter);
		
		try {
			initDB();
			String vg = crawl("http://www.vg.no/", "vg");

			if (!AvisCrawl.allArticleOld.equals(AvisCrawl.allArticle)) {

				AvisCrawl.allArticleOld.clear();
				AvisCrawl.allArticleOld.addAll(AvisCrawl.allArticle);
				AvisCrawl.allArticle.clear();
			
				AvisCrawl.setLabelText(dateNow); // stygg hack.. :/
				createArticleFolder(dateDay);
				String file = dateDay + fileSeperator + "VG-" + dateNow
						+ ".html";
				setPath(file);
				Util.WriteToFileLineByLine(filepath, dateNow + vg);
				insertArticleInDB();
			}
			AvisCrawl.allArticle.clear();
			articleURLs.clear();

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
				int counter = 0;
				for (String url : articleURLs) {
					if (isValid(url) && !exists(url, filepath)) { // vil ikke ha
																	// duplikater
																	// vil vi
																	// vel.
						prep.setString(1, url);
						prep.setString(2, filepath);
						prep.addBatch();
						counter++;
					}
				}
				prep.executeBatch();
				conn.close();
				System.out.println("\nsatt inn " + counter);

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
						AvisCrawl.allArticle.add(article);

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
			}else{
				updateDate();
				System.err.println("Did not initialize JDBC connection at task #"+ AvisCrawl.taskCounter + " at " +dateNow);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

