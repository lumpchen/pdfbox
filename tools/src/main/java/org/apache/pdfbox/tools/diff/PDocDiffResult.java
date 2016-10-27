package org.apache.pdfbox.tools.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDocDiffResult {

	private Map<Integer, PageDiffResult> entryMap;
	private DocumentInfo baseInfo;
	private DocumentInfo testInfo;
	private int diffPageCount;
	private List<Integer> diffPageNumbs;

	public PDocDiffResult() {
		this.entryMap = new HashMap<Integer, PageDiffResult>();
		this.diffPageNumbs = new ArrayList<Integer>();
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
	
	public void add(int pageNo, PageDiffResult pageResult) {
		this.entryMap.put(pageNo, pageResult);
		
		if (pageResult.count() > 0) {
			this.diffPageCount++;
			this.diffPageNumbs.add(pageNo);
		}
	}
	
	public PageDiffResult getPageDiffResult(int pageNo) {
		return this.entryMap.get(pageNo);
	}
	
	public int countOfDiffPages() {
		return this.diffPageCount;
	}
	
	public Integer[] getDiffPageNums() {
		Integer[] ret = new Integer[this.diffPageNumbs.size()];
		return this.diffPageNumbs.toArray(ret);
	}
	
	public static class DocumentInfo {
		private int pageCount;
		private String category;
		private String title;
		private String imageSuffix;
		private Map<Integer, PageInfo> pageInfoMap;
		
		public DocumentInfo() {
			this.pageInfoMap = new HashMap<Integer, PageInfo>();
		}
		
		public void setPageInfo(int page, PageInfo pageInfo) {
			this.pageInfoMap.put(page, pageInfo);
		}
		
		public PageInfo getPageInfo(int page) {
			if (page < 0 || page >= this.pageCount) {
				throw new IllegalArgumentException("Out of page range: " + page);
			}
			return this.pageInfoMap.get(page);
		}

		public int getPageCount() {
			return pageCount;
		}

		public void setPageCount(int pageCount) {
			this.pageCount = pageCount;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		
		public String getImageSuffix() {
			return imageSuffix;
		}

		public void setImageSuffix(String imageSuffix) {
			this.imageSuffix = imageSuffix;
		}
	}
	
	public static class PageInfo {
		private float width;
		private float height;
		private String previewImage;
		private int pageNo;
		
		public PageInfo(int pageNo) {
			this.pageNo = pageNo;
		}
		
		public int getPageNo() {
			return this.pageNo;
		}
		
		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			this.height = height;
		}

		public String getPreviewImage() {
			return previewImage;
		}

		public void setPreviewImage(String previewImage) {
			this.previewImage = previewImage;
		}
	}
	
}
