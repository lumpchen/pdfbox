package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.document.PageContent.AnnotContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;

public class PageThread {

	private int pageNo;
	private List<PageContent> contentList;
	
	public PageThread(int pageNo, List<PageContent> contentList) {
		this.pageNo = pageNo;
		this.contentList = contentList;
		this.analysis();
	}
	
	public static class TextLob {
		private String text;
		private Rectangle bBox;
		
		public TextLob(String text, Rectangle bBox) {
			this.text = text;
			this.bBox = bBox;
		}
		
		public String getText() {
			return this.text == null ? "" : this.text;
		}
		
		public Rectangle getBoundingBox() {
			return this.bBox;
		}
	}
	
	public static class TextThread {
		
		private StringBuilder pageText;
		private List<TextContent> textRunList;
		private List<int[]> rangeList;
		
		public TextThread() {
			this.pageText = new StringBuilder();
			this.textRunList = new ArrayList<TextContent>();
			this.rangeList = new ArrayList<int[]>();
		}
		
		public void addTextLob(TextContent textContent) {
			String text = textContent.getText();
			
			int from = this.pageText.length();
			this.pageText.append(text);
			int[] range = new int[]{from, text.length() - 1};
			this.textRunList.add(textContent);
			this.rangeList.add(range);
		}
		
		public TextLob getTextLob(String textSeg, int begin, int end) {
			int beginContentOffset = 0;
			int beginContentIndex = 0;
			int endContentOffset = 0;
			int endContentIndex = 0;
			for (int i = 0; i < this.rangeList.size(); i++) {
				int[] range = this.rangeList.get(i);
				if (begin >= range[0] && begin <= range[1]) {
					beginContentOffset = begin - range[0];
					beginContentIndex = i;
				}
				
				if (end >= range[0] && end <= range[1]) {
					endContentOffset = end - range[0];
					endContentIndex = i;
				}
			}
			
			StringBuilder buf = new StringBuilder("");
			if (beginContentIndex == endContentIndex) {
				TextContent run = this.textRunList.get(beginContentIndex);
				String text = run.getText();
				buf.append(text.substring(beginContentOffset, endContentOffset));
				Rectangle bbox = run.getBBox(beginContentOffset, endContentOffset);
				return new TextLob(buf.toString(), bbox);
			}
			
			Rectangle bbox = new Rectangle();
			for (int i = beginContentIndex; i <= endContentIndex; i++) {
				TextContent run = this.textRunList.get(i);
				String text = run.getText();
				if (i == beginContentIndex) {
					buf.append(text.substring(beginContentOffset));
					Rectangle rect = run.getBBox(beginContentOffset, text.length() - 1);
					bbox.add(rect);
					continue;
				}
				if (i == endContentIndex) {
					buf.append(text.substring(0, endContentOffset));
					Rectangle rect = run.getBBox(0, endContentOffset);
					bbox.add(rect);
					continue;
				}
				
				buf.append(text);
				Rectangle rect = run.getOutlineArea().getBounds();
				bbox.add(rect);
			}
			return new TextLob(buf.toString(), bbox);
		}
	}
	
	private void analysis() {
		if (this.contentList.isEmpty()) {
			return;
		}
		for (int i = 0; i < this.contentList.size(); i++) {
			PageContent content = this.contentList.get(i);
			int x = content.getOutlineArea().getBounds().x;
			int y = content.getOutlineArea().getBounds().y;

			if (content.getType() == PageContent.Type.Text) {
				TextContent textContent = (TextContent) content;
				
				String text = textContent.getText();
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
