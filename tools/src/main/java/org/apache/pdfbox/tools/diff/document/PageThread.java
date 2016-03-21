package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.document.PageContent.AnnotContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;

public class PageThread {

	private int pageNo;
	private List<PageContent> contentList;
	private TextThread textThread;

	public PageThread(int pageNo, List<PageContent> contentList) {
		this.pageNo = pageNo;
		this.contentList = contentList;
		this.analysis();
	}

	public TextThread getTextThread() {
		return this.textThread;
	}
	
	public static class TextLob {
		private String text;
		private Rectangle bBox;
		private TextContent content;

		public TextLob(String text, Rectangle bBox) {
			this.text = text;
			this.bBox = bBox;
		}
		
		public TextLob(String text, Rectangle bBox, TextContent content) {
			this(text, bBox);
			this.content = content;
		}


		public String getText() {
			return this.text == null ? "" : this.text;
		}

		public Rectangle getBoundingBox() {
			return this.bBox;
		}
		
		public TextContent getContent() {
			return this.content;
		}
	}

	public static class TextThread {
		
		private StringBuilder pageText;
		private List<TextSpan> textSpanList;
		private int nextBegin = 0;
		
		public TextThread() {
			this.pageText = new StringBuilder();
			this.textSpanList = new ArrayList<TextSpan>();
		}
		
		@Override
		public String toString() {
			return this.getText();
		}
		
		public String getText() {
			return this.pageText.toString();
		}
		
		public void addTextSpan(TextContent textContent) {
			String text = textContent.getText() + " ";
			this.pageText.append(text);

			TextSpan span = new TextSpan();
			span.text = text;
			span.begin = this.nextBegin;
			span.length = text.length();
			span.textContent = textContent;
			span.shapeArr = new Shape[span.length];
			List<Shape> shapeList = textContent.getOutlineShapeList();
			for (int i = 0; i < shapeList.size(); i++) {
				span.shapeArr[i] = shapeList.get(i);
			}
			this.textSpanList.add(span);
			
			nextBegin += text.length();
		}
		
		public int lenToContentEnd(int begin) {
			for (int i = 0; i < this.textSpanList.size(); i++) {
				TextSpan span = this.textSpanList.get(i);
				int[] range = new int[]{span.begin, span.begin + span.length};
				if (begin >= range[0] && begin <= range[1]) {
					return range[1] - begin + 1;
				}
			}
			return 0;
		}
		
		public TextLob[] getTextLob(int begin, int length) {
			int end = begin + length;
			int beginContentOffset = 0;
			int beginContentIndex = 0;
			int endContentOffset = 0;
			int endContentIndex = 0;
			
			for (int i = 0; i < this.textSpanList.size(); i++) {
				TextSpan span = this.textSpanList.get(i);
				
				int[] range = new int[] {span.begin, span.begin + span.length};
				if (begin >= range[0] && begin <= range[1]) {
					beginContentOffset = begin - range[0];
					beginContentIndex = i;
				}

				if (end >= range[0] && end <= range[1]) {
					endContentOffset = end - range[0];
					endContentIndex = i;
					break;
				}
			}
			
			if (beginContentIndex == endContentIndex) {
				StringBuilder buf = new StringBuilder("");
				TextSpan span = this.textSpanList.get(beginContentIndex);
				TextContent run = span.textContent;
				String text = span.text;
				buf.append(text.substring(beginContentOffset, endContentOffset));
				Rectangle bbox = span.getBBox(beginContentOffset, endContentOffset);
				return new TextLob[]{new TextLob(buf.toString(), bbox, run)};
			}

			TextLob[] list = new TextLob[endContentIndex - beginContentIndex + 1];
			for (int i = beginContentIndex; i <= endContentIndex; i++) {
				TextSpan span = this.textSpanList.get(i);
				TextContent run = span.textContent;
				if (i == beginContentIndex) {
					String text = span.text.substring(beginContentOffset);
					Rectangle rect = span.getBBox(beginContentOffset);
					list[i - beginContentIndex] = new TextLob(text, rect, run);
					continue;
				}
				if (i == endContentIndex) {
					String text = span.text.substring(0, endContentOffset);
					Rectangle rect = span.getBBox(0, endContentOffset);
					list[i - beginContentIndex] = new TextLob(text, rect, run);
					continue;
				} else {
					Rectangle rect = span.getBBox(0, span.length);
					list[i - beginContentIndex] = new TextLob(run.getText(), rect, run);
				}
			}
			return list;
		}
		
		public static class TextSpan{
			String text;
			int begin;
			int length;
			Shape[] shapeArr;
			TextContent textContent;
			
			public Rectangle getBBox(int begin) {
				return this.getBBox(begin, this.length);
			}
			public Rectangle getBBox(int begin, int end) {
				Area area = new Area();
		    	if (this.shapeArr != null) {
		    		for (int i = begin; i <= end; i++) {
		    			Shape s = this.shapeArr[i];
		    			if (s == null) {
		    				break;
		    			}
		    			if (s instanceof GeneralPath) {
		    				area.add(new Area(((GeneralPath) s).getBounds()));
		    			} else {
		    				area.add(new Area(s));    				
		    			}
		        	}
		    	}
		    	return area.getBounds();
			}
		}
	}
	

	private void analysis() {
		if (this.contentList.isEmpty()) {
			return;
		}
		this.textThread = new TextThread();
		for (int i = 0; i < this.contentList.size(); i++) {
			PageContent content = this.contentList.get(i);
			int x = content.getOutlineArea().getBounds().x;
			int y = content.getOutlineArea().getBounds().y;

			if (content.getType() == PageContent.Type.Text) {
				TextContent textContent = (TextContent) content;
				this.textThread.addTextSpan(textContent);
				
			} else if (content.getType() == PageContent.Type.Path) {
				PathContent path = (PathContent) content;
			} else if (content.getType() == PageContent.Type.Image) {
				ImageContent image = (ImageContent) content;
			} else if (content.getType() == PageContent.Type.Annot) {
				AnnotContent annot = (AnnotContent) content;
			}
		}

	}

	public int getPageNo() {
		return this.pageNo;
	}
}
