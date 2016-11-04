package org.apache.pdfbox.tools.diff;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
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
	
	public void append(DiffContent[] entries) {
		if (entries == null || entries.length == 0) {
			return;
		}
		for (DiffContent entry : entries) {
			this.append(entry);
		}
	}
	
	public List<DiffContent> getContentList() {
		return this.contentList;
	}

	public static class DiffContent {
		public static enum Category {
			Text("Text"), Image("Image"), Path("Path"), Annot("Annot");
			
			public String text;
			
			private Category(String text) {
				this.text = text;
			}
		};
		
		public static class Key {
			public static String Attr_Pos_X = "X Position";
			public static String Attr_Pos_Y = "Y Position";
			
			public static String Attr_Text = "Text";
			public static String Attr_Font = "Font";
			public static String Attr_Font_size = "Font Size";
			public static String Attr_Stroke_Colorspace = "Stroke Colorspace";
			public static String Attr_Stroke_Color = "Stroke Color";
			public static String Attr_Fill_Colorspace = "Fill Colorspace";
			public static String Attr_Fill_Color = "Fill Color";
			
			public static String Attr_Painting_OP = "Paint Operator";
			public static String Attr_Line_Width = "Line Width";
			public static String Attr_Line_Cap = "Line Cap";
			public static String Attr_Line_Join = "Line Join";
			public static String Attr_Miter_Limit = "Miter Limit";
			
			public static String Attr_Width = "Width";
			public static String Attr_Height = "Height";
			public static String Attr_Byte_count = "Byte Count";
			public static String Attr_Bits_Per_Component = "BitsPerComponent";
			public static String Attr_Frame_size = "Frame Size";
			public static String Attr_Decode = "Decode";
			public static String Attr_Suffix = "Suffix";
			
			public static String Attr_SubType = "SubType";
			public static String Attr_FieldType = "FieldType";
			public static String Attr_AnnotName = "AnnotName";
			public static String Attr_AnnotContents = "AnnotContents";
			public static String Attr_Annot_Rect = "Rectangle";
			public static String Attr_Annot_Appearance = "Appearance";
		}
				
		private Category category;
		private List<ContentAttr> contentAttrList;
		private Area baseOutline;
		private Area testOutline;
		private Rectangle2D baseBBox, testBBox;
		private List<Rectangle2D> baseSubBBox, testSubBBox;
		
		public DiffContent(Category category) {
			this.category = category;
			this.contentAttrList = new ArrayList<ContentAttr>();
		}
		
		public Category getCategory() {
			return this.category;
		}
		
		public List<ContentAttr> getAttrList() {
			return this.contentAttrList;
		}
		
		public void setOutline(Area baseOutline, Area testOutline) {
			this.baseOutline = baseOutline;
			this.testOutline = testOutline;
		}
		
		public Rectangle getBaseOutlineRect() {
			if (this.baseOutline != null) {
				return this.baseOutline.getBounds();
			}
			return null;
		}
		
		public Rectangle getTestOutlineRect() {
			if (this.testOutline != null) {
				return this.testOutline.getBounds();
			}
			return null;
		}
		
		public void setBBox(Rectangle2D baseBBox, Rectangle2D testBBox) {
			this.baseBBox = baseBBox;
			this.testBBox = testBBox;
		}
		
		public void setSubBBox(List<Rectangle2D> baseSubBBox, List<Rectangle2D> testSubBBox) {
			this.baseSubBBox = baseSubBBox;
			this.testSubBBox = testSubBBox;
		}
		
		public Rectangle2D getBaseBBox() {
			return this.baseBBox;
		}
		
		public Rectangle2D getTestBBox() {
			return this.testBBox;
		}
		
		public List<Rectangle2D> getBaseSubBBox() {
			return this.baseSubBBox;
		}
		
		public List<Rectangle2D> getTestSubBBox() {
			return this.testSubBBox;
		}
		
		private void putAttr(String key, boolean equals, String baseVal, String testVal) {
			ContentAttr attr = new ContentAttr();
			attr.key = key;
			attr.equals = equals;
			attr.baseVal = baseVal;
			attr.testVal = testVal;
			this.contentAttrList.add(attr);
		}
		
		public void putAttr(String key, boolean equals, Object baseVal, Object testVal) {
			String baseStr = baseVal == null ? "" : baseVal.toString();
			String testStr = testVal == null ? "" : testVal.toString();
			this.putAttr(key, equals, baseStr, testStr);
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			for (ContentAttr attr : this.contentAttrList) {
				buf.append(attr.key);
				buf.append(" | ");
				buf.append(attr.equals);
				buf.append(" | ");
				buf.append(attr.baseVal == null ? "null" : attr.baseVal);
				buf.append(" | ");
				buf.append(attr.testVal == null ? "null" : attr.testVal);
				
				buf.append("\n");
			}
			
			return buf.toString();
		}
	}
	
	public static class ContentAttr {
		public String key;
		public boolean equals;
		public String baseVal;
		public String testVal;
	}
	
}
