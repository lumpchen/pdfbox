package org.apache.pdfbox.tools.diff;

import java.util.ArrayList;
import java.util.List;

public class PageDiffResult {
	
	private List<DiffContent> contentList;
	
	public PageDiffResult() {
		this.contentList = new ArrayList<DiffContent>();
	}

	public int count() {
		return this.contentList.size();
	}
	
	public void append(DiffContent entry) {
		this.contentList.add(entry);
	}

	public static class DiffContent {
		public static enum Category {
			Text, Image, Path, Annot
		};
		
		public static class Key {
			public static String Attr_Text ="Text";
			public static String Attr_Font ="Font";
			public static String Attr_Font_size ="Font size";
			public static String Attr_Colorspace ="Colorspace";
			public static String Attr_Color ="Color";
		}
				
		private Category category;
		private List<ContentAttr> contentAttrList;
		
		public DiffContent(Category category) {
			this.category = category;
		}
		
		public Category getCategory() {
			return this.category;
		}
		
		public void putAttr(String key, boolean equals, String baseVal, String testVal) {
			ContentAttr attr = new ContentAttr();
			attr.key = key;
			attr.equals = equals;
			attr.baseVal = baseVal;
			attr.testVal = testVal;
			this.contentAttrList.add(attr);
		}
	}
	
	public static class ContentAttr {
		public String key;
		public boolean equals;
		public String baseVal;
		public String testVal;
	}
	
}
