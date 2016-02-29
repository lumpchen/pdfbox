package org.apache.pdfbox.tools.diff;

import java.util.HashMap;
import java.util.Map;

public class PDocDiffResult {

	private Map<Integer, PageDiffResult> entryMap;
	private DocumentInfo baseInfo;
	private DocumentInfo testInfo;
	private int diffPageCount;

	public PDocDiffResult() {
		this.entryMap = new HashMap<Integer, PageDiffResult>();
	}

	public void setDocumentInfo(DocumentInfo baseInfo, DocumentInfo testInfo) {
		this.baseInfo = baseInfo;
		this.testInfo = testInfo;
	}
	
	public DocumentInfo getBaseDocumentInfo() {
		if (this.baseInfo == null) {
			this.baseInfo = new DocumentInfo();
		}
		return this.baseInfo;
	}
	
	public DocumentInfo getTestDocumentInfo() {
		if (this.testInfo == null) {
			this.testInfo = new DocumentInfo();
		}
		return this.testInfo;
	}
	
	public static class DocumentInfo {
		public int pageCount;
		public String category;
		public String title;
	}
	
	public void add(int pageNo, PageDiffResult pageResult) {
		this.entryMap.put(pageNo, pageResult);
		
		if (pageResult.count() > 0) {
			this.diffPageCount++;
		}
	}
	
	public int countOfDiffPages() {
		return this.diffPageCount;
	}
	
}
