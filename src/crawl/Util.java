package crawl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static String fileSeperator = System.getProperty("file.separator");
    
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
    
    public static boolean isNumeric(String aStringValue) {
	Pattern pattern = Pattern.compile("\\d+");
	Matcher matcher = pattern.matcher(aStringValue);
	return matcher.matches();
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
    
    public static void generateCSSFolder(String pathToFolder) {
	try {
		Util.WriteToFileLineByLine(pathToFolder + fileSeperator + "css"
			+ fileSeperator + "general.css",
			Util.getURLContent("http://www.vg.no/css/general.css"));
		Util.WriteToFileLineByLine(pathToFolder + fileSeperator + "css"
			+ fileSeperator + "frontpage.css",
			Util.getURLContent("http://www.vg.no/css/frontpage.css"));
		Util.WriteToFileLineByLine(pathToFolder + fileSeperator + "css"
			+ fileSeperator + "thickbox.css",
			Util.getURLContent("http://www.vg.no/css/thickbox.css"));
		Util.WriteToFileLineByLine(
			pathToFolder + fileSeperator + "css" + fileSeperator
				+ "horizontalMenu.css",
				Util.getURLContent("http://www.vg.no/css/horizontalMenu.css"));
	    } catch (IOException e) {
		e.printStackTrace();
	    }		
    }
}
