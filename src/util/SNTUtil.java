package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SNTUtil {
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
}
