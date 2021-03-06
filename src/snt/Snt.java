package snt;

import java.io.File;
import java.util.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Snt {

	public static String latestHash;

	public Snt(long intervall) {

		Document doc = null;
		try {
			doc = Jsoup.connect("http://vg.no").get();

			Elements artikler = doc.select(".article-content");
			for (Element tikkel : artikler) {
				Elements imgTags = tikkel
						.select(".df-img-container-inner a img");
				for (Element e : imgTags) {
					e.attr("src", e.attr("src").substring(9)); // fjern variable
																// data i url
				}
			}
			latestHash = "md5hash"; // MD5(artikler);

			Timer t = new Timer();
			t.scheduleAtFixedRate(new SntTask(), 0, intervall);
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		int input = 30000;
		if (args.length > 0) {
			try {
				input = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				System.out.println("Usage: java -jar <filename>.jar <intervall in milliseconds>\nor without arguments for a default 30 sec intervall.");
				System.exit(-1);
			}
		}
		System.out.println("Intervall: " + input);
		new Snt(input);
	}

	public static String MD5(Object obj)
			throws java.security.NoSuchAlgorithmException {
		String buffer = obj.toString();
		java.security.MessageDigest digest = java.security.MessageDigest
				.getInstance("MD5");
		digest.update(buffer.getBytes());
		byte[] b = digest.digest();
		buffer = "";

		for (int i = 0; i < b.length; i++) {
			buffer += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}

		return buffer;
	}

}
