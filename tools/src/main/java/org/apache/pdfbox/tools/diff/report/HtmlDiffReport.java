package org.apache.pdfbox.tools.diff.report;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.tools.diff.PDocDiffResult;
import org.apache.pdfbox.tools.diff.PDocDiffResult.DocumentInfo;
import org.apache.pdfbox.tools.diff.PDocDiffResult.PageInfo;
import org.apache.pdfbox.tools.diff.PageDiffResult;
import org.apache.pdfbox.tools.diff.PageDiffResult.ContentAttr;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.json.JSONArray;
import org.json.JSONObject;

public class HtmlDiffReport {

	private static final String ResourcePackage = "org/apache/pdfbox/tools/diff/report/html/";

	private static final String diff_report_data_filename = "diff_report_data.js";
	private static final String diff_report_data_ns = "PDF_DIFF";
	private static final String diff_report_data = "diff_report_data";
	
	private static final String diff_page_count = "diff_page_count";
	private static final String diff_page_nums = "diff_page_nums";
	private static final String base_pdf_json_obj = "base_pdf_json_obj";
	private static final String test_pdf_json_obj = "test_pdf_json_obj";
	private static final String diff_content_json_obj = "diff_content_json_obj";
	
	private static final String Rendering_Resolution = "Rendering_Resolution";
	private static final String Base_Stroke_Color = "Base_Stroke_Color";
	private static final String Test_Stroke_Color = "Test_Stroke_Color";
	private static final String Test_Fill_Color = "Test_Fill_Color";
	private static final String Base_Fill_Color = "Base_Fill_Color";

	private File imageDir;
	private File baseDir;
	private String mainName;
	private PDocDiffResult result;

	public HtmlDiffReport(File baseDir, String name, PDocDiffResult result) throws IOException {
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
		InputStream htmlTemplate = null;
		try {
			htmlTemplate = loadTemplate("html_report_template.html");
			copyFile(htmlTemplate, this.mainName + ".html", this.baseDir);
		} finally {
			if (htmlTemplate != null) {
				htmlTemplate.close();
			}
		}
	}

	private void writeCss() throws IOException {
		this.copyTemplate("report_styles.css", "css");
		this.copyTemplate("bootstrap.css", "css");
		this.copyTemplate("bootstrap-treeview.css", "css");
		
		this.copyTemplate("glyphicons-halflings-regular.woff2", "fonts");
		this.copyTemplate("glyphicons-halflings-regular.woff", "fonts");
		this.copyTemplate("glyphicons-halflings-regular.ttf", "fonts");
	}

	private void writeJS() throws IOException {
		this.copyTemplate("jquery.js", "js");
		this.copyTemplate("bootstrap-treeview.js", "js");
		this.copyTemplate("diff_report_view.js", "js");

		this.writeDataJS();
	}

	private void writeDataJS() throws IOException {
		String path = this.baseDir.getAbsolutePath() + "/js/" + diff_report_data_filename;
		File dataJS = new File(path);
		if (!dataJS.exists()) {
			if (!dataJS.createNewFile()) {
				throw new IOException("Cannot create report data js file: " + path);
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dataJS);

			StringBuilder data = new StringBuilder();
			
			data.append("var " + diff_report_data_ns + " = " + diff_report_data_ns + " || {};");
			data.append("\n");
			data.append(diff_report_data_ns + "." + diff_report_data + " = ");
			
			data.append(this.toJSonString());
			fos.write(data.toString().getBytes("UTF-8"));
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	private String toJSonString() throws IOException {
		JSONObject json = new JSONObject();
		
		// change by setting
		json.put(Rendering_Resolution, this.result.getResolution());
    	json.put(Base_Stroke_Color, "red");
		json.put(Test_Stroke_Color, "red");
		json.put(Test_Fill_Color, "rgba(138, 43, 226, 0.2)");
		json.put(Base_Fill_Color, "rgba(138, 43, 226, 0.2)");
	
		json.put(diff_page_count, this.result.countOfDiffPages());
		json.put(diff_page_nums, this.result.getDiffPageNums());
		json.put(base_pdf_json_obj, this.toJSon(this.result.getBaseDocumentInfo()));
		json.put(test_pdf_json_obj, this.toJSon(result.getTestDocumentInfo()));
		json.put(diff_content_json_obj, this.toJSon(result));

		return json.toString(4);
	}

	private static void writeTo(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int read;
		while ((read = is.read(buf)) != -1) {
			os.write(buf, 0, read);
		}
	}

	private static InputStream loadTemplate(String name) {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ResourcePackage + name);
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
			if (pageInfo != null) {
				JSONObject pageJson = this.toJSon(pageInfo, tagPrefix, tagSuffix);
				pageArray.put(pageJson);
			} else {
				pageArray.put(new JSONObject());
			}
		}
		docJson.put("pages", pageArray);
		return docJson;
	}

	private JSONArray toJSon(PDocDiffResult docResult) throws IOException {
		JSONArray result = new JSONArray();
		Integer[] nums = docResult.getDiffPageNums();

		for (int i : nums) {
			JSONObject pageEntry = new JSONObject();
			PageDiffResult pageResult = docResult.getPageDiffResult(i);
			pageEntry.put("PageNo", i);
			JSONObject obj = this.toJSon(pageResult);
			pageEntry.put("Result", obj);
			result.put(pageEntry);
		}
		return result;
	}

	private JSONObject toJSon(PageDiffResult pageResult) throws IOException {
		JSONObject json = new JSONObject();
		List<DiffContent> contentList = pageResult.getContentList();

		JSONArray textArr = new JSONArray();
		JSONArray imageArr = new JSONArray();
		JSONArray pathArr = new JSONArray();
		JSONArray annotArr = new JSONArray();
		for (DiffContent content : contentList) {
			JSONObject obj = this.toJSon(content);
			if (content.getCategory() == DiffContent.Category.Text) {
				textArr.put(obj);
			}

			if (content.getCategory() == DiffContent.Category.Image) {
				imageArr.put(obj);
			}

			if (content.getCategory() == DiffContent.Category.Path) {
				pathArr.put(obj);
			}

			if (content.getCategory() == DiffContent.Category.Annot) {
				annotArr.put(obj);
			}
		}
		json.put(DiffContent.Category.Text.text, textArr);
		json.put(DiffContent.Category.Image.text, imageArr);
		json.put(DiffContent.Category.Path.text, pathArr);
		json.put(DiffContent.Category.Annot.text, annotArr);
		return json;
	}

	private JSONObject toJSon(DiffContent diffContent) throws IOException {
		JSONObject json = new JSONObject();
		List<ContentAttr> attrs = diffContent.getAttrList();
		JSONArray attrArr = new JSONArray();
		for (ContentAttr attr : attrs) {
			JSONObject attrMap = new JSONObject();
			attrMap.put("Key", attr.key);
			attrMap.put("Equals", attr.equals);
			JSONArray arr = new JSONArray();
			arr.put(attr.baseVal == null ? "" : attr.baseVal);
			arr.put(attr.testVal == null ? "" : attr.testVal);
			attrMap.put("Value", arr);
			attrArr.put(attrMap);
		}
		json.put("Attributes", attrArr);

		JSONArray arr = new JSONArray();
		JSONArray sRect = new JSONArray();
		if (diffContent.getBaseBBox() != null) {
			Rectangle2D rect = diffContent.getBaseBBox();
			sRect.put(rect.getX());
			sRect.put(rect.getY());
			sRect.put(rect.getWidth());
			sRect.put(rect.getHeight());
		}
		arr.put(sRect);

		sRect = new JSONArray();
		if (diffContent.getTestBBox() != null) {
			Rectangle2D rect = diffContent.getTestBBox();
			sRect.put(rect.getX());
			sRect.put(rect.getY());
			sRect.put(rect.getWidth());
			sRect.put(rect.getHeight());
		}
		arr.put(sRect);

		json.put("Outline", arr);
		return json;
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
	
	private void copyTemplate(String resName, String folderName) throws IOException {
		InputStream srcFile = null;
		try {
			String subPath = folderName == null || folderName.isEmpty() ? resName : folderName + "/" + resName;
			srcFile = loadTemplate(subPath);

			File dest = this.baseDir;
			if (folderName != null && !folderName.isEmpty()) {
				dest = new File(this.baseDir, folderName);
			}
			copyFile(srcFile, resName, dest);
		} finally {
			if (srcFile != null) {
				srcFile.close();
			}
		}
	}

	private static void copyFile(InputStream src, String fileName, File dstDir) throws IOException {
		if (!dstDir.exists()) {
			if (!dstDir.mkdir()) {
				throw new IOException("Cannot create directory: " + dstDir);
			}
		}
		
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

}
