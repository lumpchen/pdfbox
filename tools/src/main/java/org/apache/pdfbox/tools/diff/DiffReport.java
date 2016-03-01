package org.apache.pdfbox.tools.diff;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.tools.diff.PDocDiffResult.DocumentInfo;
import org.apache.pdfbox.tools.diff.PDocDiffResult.PageInfo;
import org.json.JSONArray;
import org.json.JSONObject;

public class DiffReport {

	private static final String baseline_pdf_name_placeholder = "&baseline_pdf_name&";
	private static final String test_pdf_name_placeholder = "&test_pdf_name&";
	private static final String diff_page_count_placeholder = "&diff_page_count&";
	private static final String page_count_placeholder = "&page_count&";
	private static final String diff_page_nums_placeholder = "&diff_page_nums&";
	private static final String baseline_page_images_placeholder = "&baseline_page_images&";
	private static final String test_page_images_placeholder = "&test_page_images&";
	
	private File imageDir;
	private File baseDir;
	private String mainName;
	private PDocDiffResult result;
	
	public DiffReport(File baseDir, String name, PDocDiffResult result) throws IOException {
		this.baseDir = baseDir;
		if (!this.baseDir.isDirectory()) {
			throw new IOException("Not a folder: " + this.baseDir.getAbsolutePath());
		}
		String path = baseDir.getAbsolutePath() + "/" + "images";
		this.imageDir = new File(path);
		if (!imageDir.exists()) {
			if (!imageDir.mkdir()) {
				throw new IOException("Cannot create /images: " + imageDir);
			}
		}
		this.mainName = name;
		this.result = result;
	}
	
	public void toHtml() throws IOException {
		writeHtml();
		writeCss();
		writeJS();
	}
	
	private void writeHtml() throws IOException {
		String path = this.baseDir.getAbsolutePath() + "/" + this.mainName + ".html";
		File main = new File(path);
		if (!main.exists()) {
			if (!main.createNewFile()) {
				throw new IOException("Cannot create html file: " + main);
			}
		}
		InputStream htmlTemplate = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(main);
			htmlTemplate = loadHtmlTemplate();
			writeTo(htmlTemplate, fos);
		} finally {
			if (htmlTemplate != null) {
				htmlTemplate.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	private void writeCss() throws IOException {
		String path = this.baseDir.getAbsolutePath() + "/" + "report_styles.css";
		File css = new File(path);
		if (!css.exists()) {
			if (!css.createNewFile()) {
				throw new IOException("Cannot create css file: " + path);
			}
		}
		InputStream cssTemplate = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(css);
			
			cssTemplate = loadCssTemplate();
			writeTo(cssTemplate, fos);
		} finally {
			if (cssTemplate != null) {
				cssTemplate.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	private void writeJS() throws IOException {
		String path = this.baseDir.getAbsolutePath() + "/" + "pdf_diff_report.js";
		File js = new File(path);
		if (!js.exists()) {
			if (!js.createNewFile()) {
				throw new IOException("Cannot create js file: " + path);
			}
		}
		InputStream jsTemplate = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(js);
			
			jsTemplate = loadJSTemplate();
			String rep = remplaceVariable(jsTemplate);
			fos.write(rep.getBytes("UTF-8"));
		} finally {
			if (jsTemplate != null) {
				jsTemplate.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	private String remplaceVariable(InputStream jsStream) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		writeTo(jsStream, bos);
		byte[] bytes = bos.toByteArray();
		
		String s = new String(bytes, "UTF-8");
		
		s = s.replaceFirst(baseline_pdf_name_placeholder, "\"" + result.getBaseDocumentInfo().getTitle() + "\"");
		s = s.replaceFirst(test_pdf_name_placeholder, "\"" + result.getTestDocumentInfo().getTitle() + "\"");
		s = s.replaceFirst(diff_page_count_placeholder, result.countOfDiffPages() + "");
		s = s.replaceFirst(page_count_placeholder, result.getBaseDocumentInfo().getPageCount() + "");
		
		Integer[] diffPageNums = result.getDiffPageNums();
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i = 0; i < diffPageNums.length; i++) {
			buf.append(diffPageNums[i]);
			if (i != diffPageNums.length - 1) {
				buf.append(", ");
			}
		}
		buf.append("]");
		s = s.replaceFirst(diff_page_nums_placeholder, buf.toString());
		
		String baseline_page_images = writeImages(result.getBaseDocumentInfo(), "base", 
				"." + result.getBaseDocumentInfo().getImageSuffix());
		s = s.replaceFirst(baseline_page_images_placeholder, baseline_page_images);
		
		String test_page_images = writeImages(result.getTestDocumentInfo(), "test", 
				"." + result.getTestDocumentInfo().getImageSuffix());
		s = s.replaceFirst(test_page_images_placeholder, test_page_images);
		return s;
	}
	
	private String writeImages(DocumentInfo docInfo, String tagPrefix, String tagSuffix) throws IOException {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		int n = docInfo.getPageCount();
		for (int i = 0; i < n; i++) {
			String path = docInfo.getPageInfo(i).getPreviewImage();
			File image = new File(path);
			if (image.exists()) {
				String imageTag = tagPrefix + "-" + i + tagSuffix;
				File imageFile = new File(this.imageDir.getAbsolutePath() + "/" + imageTag);
				if (!imageFile.exists()) {
					if (!imageFile.createNewFile()) {
						throw new IOException("Cannot create preview image file: " + imageFile.getAbsolutePath());						
					}
				}
				writeTo(new FileInputStream(image), new FileOutputStream(imageFile));
				buf.append("\"" + imageTag + "\"");
			}
			
			if (i != n - 1) {
				buf.append(", ");
			}
		}
		buf.append("]");
		return buf.toString();
	}
	
	private static void writeTo(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int read;
		while ((read = is.read(buf)) != -1) {
			os.write(buf, 0, read);
		}
	}
	
	private static InputStream loadHtmlTemplate() {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("org/apache/pdfbox/tools/diff/html_report_template.html");
		return is;
	}

	private static InputStream loadCssTemplate() {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("org/apache/pdfbox/tools/diff/report_styles.css");
		return is;
	}

	private static InputStream loadJSTemplate() {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("org/apache/pdfbox/tools/diff/pdf_diff_report.js");
		return is;
	}
	
	private static String toJSon(PDocDiffResult result) {
		DocumentInfo docInfo = result.getBaseDocumentInfo();
		JSONObject jo = new JSONObject();
		
		
		return "";
	}
	
	private static String toJSon(DocumentInfo docInfo) {
		
		return "";
	}
	
	private static String toJSon(PageInfo pageInfo) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("pageNo", pageInfo.getPageNo() + "");
		
		map.put("width", pageInfo.getWidth() + "");
		map.put("height", pageInfo.getHeight() + "");
		
		
		return "";
	}
	
	
	static String toJson(PDocDiffResult result) {
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

	public static void main(String[] args) {
		toJson(null);
	}
}
