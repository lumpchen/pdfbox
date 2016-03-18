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
		private List<TextContent> textRunList;
		private List<int[]> rangeList;

		public TextThread() {
			this.pageText = new StringBuilder();
			this.textRunList = new ArrayList<TextContent>();
			this.rangeList = new ArrayList<int[]>();
		}
		
		@Override
		public String toString() {
			return this.getText();
		}
		
		public String getText() {
			return this.pageText.toString();
		}

		public void addTextLob(TextContent textContent) {
			String text = textContent.getText();

			int from = this.pageText.length();
			this.pageText.append(text);
			this.pageText.append(" ");
			
			int[] range = new int[] {from, text.length() + from + 1};
			this.textRunList.add(textContent);
			this.rangeList.add(range);
		}

		public TextLob[] getTextLob(int begin, int end) {
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
					break;
				}
			}

			if (beginContentIndex == endContentIndex) {
				StringBuilder buf = new StringBuilder("");
				TextContent run = this.textRunList.get(beginContentIndex);
				String text = run.getText();
				buf.append(text.substring(beginContentOffset, endContentOffset - 1));
				Rectangle bbox = run.getBBox(beginContentOffset, endContentOffset - 1);
				return new TextLob[]{new TextLob(buf.toString(), bbox, run)};
			}

			TextLob[] list = new TextLob[endContentIndex - beginContentIndex + 1];
			for (int i = beginContentIndex; i <= endContentIndex; i++) {
				TextContent run = this.textRunList.get(i);
				if (i == beginContentIndex) {
					String text = run.getText().substring(beginContentOffset);
					Rectangle rect = run.getBBox(beginContentOffset, run.getText().length() - 1);
					list[i - beginContentIndex] = new TextLob(text, rect);
					continue;
				}
				if (i == endContentIndex) {
					String text = run.getText().substring(0, endContentOffset - 1);
					Rectangle rect = run.getBBox(0, endContentOffset - 1);
					list[i - beginContentIndex] = new TextLob(text, rect);
					continue;
				} else {
					Rectangle rect = run.getOutlineArea().getBounds();
					list[i - beginContentIndex] = new TextLob(run.getText(), rect);
				}
			}
			return list;
		}
		
		public int lenToContentEnd(int begin) {
			for (int i = 0; i < this.rangeList.size(); i++) {
				int[] range = this.rangeList.get(i);
				if (begin >= range[0] && begin < range[1]) {
					return range[1] - begin;
				}
			}
			return 0;
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
				this.textThread.addTextLob(textContent);
				
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
