package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class DiffReport {
	
	public static void main(String[] args) throws IOException {
//		InputStream is = Thread.currentThread().getContextClassLoader().
//				getResourceAsStream("org/apache/pdfbox/tools/diff/html_report_template.html");
//		System.out.println(is.available());
		
		reportHtml(null, null);
	}

	public static void reportHtml(File dir, PDocDiffResult result) throws IOException {
		StringWriter writer = new StringWriter();
		writeln(writer, "<!DOCTYPE HTML>");
		writeln(writer, "<html>");
		
		writeln(writer, "<head>");
		writeln(writer, "<title>PDF DIFF Report</title>");
		writeln(writer, "<script type=\"text/javascript\">");
		writeScript(writer);
		writeln(writer, "</script>");
		writeln(writer, "</head>");
		
		writeln(writer, "<body>");
		writeSummary(writer, result);
		writeln(writer, "</body>");
	
		writeln(writer, "</html>");

		System.out.println(writer.getBuffer().toString());
		
		writeToFile(dir, writer.getBuffer().toString(), result);
	}
	
	static void writeScript(StringWriter writer) {
		
	}
	
	static void writeSummary(StringWriter writer, PDocDiffResult result) {
		writeln(writer, "<h1>");
		writeln(writer, "Baseline: " + result.getBaseDocumentInfo().title);
		writeln(writer, "</h1>");
		
		writeln(writer, "<h1>");
		writeln(writer, "Test: " + result.getTestDocumentInfo().title);
		writeln(writer, "</h1>");
		
		writeln(writer, "<h1>");
		if (result.countOfDiffPages() == 0) {
			writeln(writer, "The 2 PDFs are same!");
		} else if (result.countOfDiffPages() == 1) {
			writeln(writer, "Found " + result.countOfDiffPages() + " different page!");
		} else {
			writeln(writer, "Found " + result.countOfDiffPages() + " different pages!");	
		}
		
		writeln(writer, "</h1>");
	}

	static void writeToFile(File dir, String html, PDocDiffResult result) throws IOException {
		if (!dir.isDirectory()) {
			throw new IOException("Not a folder: " + dir.getAbsolutePath());
		}
		
		String path = dir.getAbsolutePath() + "/" + result.getBaseDocumentInfo().title + ".html";
		File main = new File(path);
		if (!main.exists()) {
			if (!main.createNewFile()) {
				throw new IOException("Cannot create html file: " + main);
			}			
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(main);
			fos.write(html.getBytes("utf-8"));			
		} finally {
			if (fos != null) {
				fos.close();				
			}
		}
	}
	
	static void writeln(StringWriter writer, String text) {
		writer.write(text);
		writer.write("\n");
	}

	private static String toJson(PDocDiffResult result) {
		JSONObject jo = new JSONObject();

		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("name", "Alexia");
		map1.put("sex", "female");
		map1.put("age", "23");

		Map<String, String> map2 = new HashMap<String, String>();
		map2.put("name", "Edward");
		map2.put("sex", "male");
		map2.put("age", "24");

		List<Map> list = new ArrayList<Map>();
		list.add(map1);
		list.add(map2);

		JSONArray ja = new JSONArray();
		ja.put(list);

		System.out.println(ja.toString());


		return jo.toString();
	}

}
