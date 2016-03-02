package org.apache.pdfbox.tools.diff.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.tools.diff.PDocDiffResult;
import org.apache.pdfbox.tools.diff.PDocDiffResult.DocumentInfo;
import org.apache.pdfbox.tools.diff.PDocDiffResult.PageInfo;
import org.json.JSONArray;
import org.json.JSONObject;

public class DiffReport {

	private static final String ResourcePackage =  "org/apache/pdfbox/tools/diff/report/html/";
	
	private static final String diff_page_count_placeholder = "&diff_page_count&";
	private static final String diff_page_nums_placeholder = "&diff_page_nums&";
	private static final String base_pdf_json_placeholder = "&base_pdf_json&";
	private static final String test_pdf_json_placeholder = "&test_pdf_json&";
	
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
			htmlTemplate = loadTemplate("html_report_template.html");
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
		this.writeCss("report_styles.css");
		this.writeCss("bootstrap.css");
	}
	
	private void writeCss(String cssName) throws IOException {
		InputStream cssTemplate = null;
		try {
			cssTemplate = loadTemplate(cssName);
			copyFile(cssTemplate, cssName, this.baseDir);
		} finally {
			if (cssTemplate != null) {
				cssTemplate.close();
			}
		}
	}
	
	private static void copyFile(InputStream src, String fileName, File dstDir) throws IOException {
		String dst = dstDir.getAbsolutePath() + "/" + fileName;
		File dstFile = new File(dst);
		if (!dstFile.exists()) {
			if (!dstFile.createNewFile()) {
				throw new IOException("Cannot create file: " + dst);
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dstFile);
			writeTo(src, fos);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	private void writeJS() throws IOException {
		this.writeJS("jquery.js");
		this.writeJS("bootstrap-treeview.js");
		this.writeReportJS();
	}
	
	private void writeJS(String jsName) throws IOException {
		InputStream jsTemplate = null;
		try {
			jsTemplate = loadTemplate(jsName);
			copyFile(jsTemplate, jsName, this.baseDir);
		} finally {
			if (jsTemplate != null) {
				jsTemplate.close();
			}
		}
	}
	
	private void writeReportJS() throws IOException {
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
			
			jsTemplate = loadTemplate("pdf_diff_report.js");
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
		
		s = s.replaceFirst(diff_page_count_placeholder, result.countOfDiffPages() + "");
		
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
		
		String base_pdf_json = this.toJSon(result.getBaseDocumentInfo()).toString();
		s = s.replaceFirst(base_pdf_json_placeholder, "\'" + base_pdf_json + "\'");
		
		String test_pdf_json = this.toJSon(result.getTestDocumentInfo()).toString();
		s = s.replaceFirst(test_pdf_json_placeholder, "\'" + test_pdf_json + "\'");
		
		return s;
	}
	
	private static void writeTo(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int read;
		while ((read = is.read(buf)) != -1) {
			os.write(buf, 0, read);
		}
	}
	
	private static InputStream loadTemplate(String name) {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(ResourcePackage + name);
		return is;
	}

	private JSONObject toJSon(DocumentInfo docInfo) throws IOException {
		JSONObject docJson = new JSONObject();
		docJson.put("pageCount", docInfo.getPageCount());
		docJson.put("title", docInfo.getTitle());
		
		String tagPrefix = docInfo.getCategory();
		String tagSuffix = docInfo.getImageSuffix();
		
		JSONArray pageArray = new JSONArray();
		for (int i = 0; i < docInfo.getPageCount(); i++) {
			PageInfo pageInfo = docInfo.getPageInfo(i);
			JSONObject pageJson = this.toJSon(pageInfo, tagPrefix, tagSuffix);
			pageArray.put(pageJson);
		}
		docJson.put("pages", pageArray);
		return docJson;
	}
	
	private String writeImages(PageInfo pageInfo, String tagPrefix, String tagSuffix) throws IOException {
		String path = pageInfo.getPreviewImage();
		File image = new File(path);
		if (image.exists()) {
			String imageTag = tagPrefix + "-" + pageInfo.getPageNo() + "." + tagSuffix;
			File imageFile = new File(this.imageDir.getAbsolutePath() + "/" + imageTag);
			if (!imageFile.exists()) {
				if (!imageFile.createNewFile()) {
					throw new IOException("Cannot create preview image file: " + imageFile.getAbsolutePath());						
				}
			}
			writeTo(new FileInputStream(image), new FileOutputStream(imageFile));
			return imageTag;
		}
		return "";
	}
	
	private JSONObject toJSon(PageInfo pageInfo, String tagPrefix, String tagSuffix) throws IOException {
		JSONObject map = new JSONObject();
		map.put("num", pageInfo.getPageNo());
		map.put("width", pageInfo.getWidth());
		map.put("height", pageInfo.getHeight());
		String imageTag = this.writeImages(pageInfo, tagPrefix, tagSuffix);
		map.put("imageTag", imageTag);
		
		return map;
	}
}
