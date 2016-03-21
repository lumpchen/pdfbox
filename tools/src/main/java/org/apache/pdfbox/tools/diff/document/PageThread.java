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
		
		public void addTextLob(TextContent textContent) {
			String text = textContent.getText() + " ";
			this.pageText.append(text);

			TextSpan span = new TextSpan();
			span.text = text;
			span.begin = this.nextBegin;
			span.length = text.length();
			span.textContent = textContent;
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
					endContentOffset = end - range[0] - 1;
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
				Rectangle bbox = run.getBBox(beginContentOffset, endContentOffset);
				return new TextLob[]{new TextLob(buf.toString(), bbox, run)};
			}

			TextLob[] list = new TextLob[endContentIndex - beginContentIndex + 1];
			for (int i = beginContentIndex; i <= endContentIndex; i++) {
				TextSpan span = this.textSpanList.get(i);
				TextContent run = span.textContent;
				if (i == beginContentIndex) {
					String text = span.text.substring(beginContentOffset);
					Rectangle rect = run.getBBox(beginContentOffset, run.getText().length() - 1);
					list[i - beginContentIndex] = new TextLob(text, rect);
					continue;
				}
				if (i == endContentIndex) {
					String text = span.text.substring(0, endContentOffset);
					Rectangle rect = run.getBBox(0, endContentOffset);
					list[i - beginContentIndex] = new TextLob(text, rect);
					continue;
				} else {
					Rectangle rect = run.getOutlineArea().getBounds();
					list[i - beginContentIndex] = new TextLob(run.getText(), rect);
				}
			}
			return list;
		}
		
		public static class TextSpan{
			String text;
			int begin;
			int length;
			TextContent textContent;
		}
	}
	
	public static class TextThread_2 {

		private StringBuilder pageText;
		private List<TextContent> textRunList;
		private List<int[]> rangeList;

		public TextThread_2() {
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

		private int nextBegin = 0;
		public void addTextLob(TextContent textContent) {
			String text = textContent.getText();

			int from = nextBegin;
			this.pageText.append(text);
			this.pageText.append(" ");
			int len = text.length();
			
			int[] range = new int[] {from, len + from};
			nextBegin += len + 1;
			
			this.textRunList.add(textContent);
			this.rangeList.add(range);
		}
		
		public TextLob[] getTextLob(int begin, int len) {
			int end = begin + len - 1;
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
				String text = run.getText() + " ";
				buf.append(text.substring(beginContentOffset, endContentOffset));
				Rectangle bbox = run.getBBox(beginContentOffset, endContentOffset);
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
				if (begin >= range[0] && begin <= range[1]) {
					return range[1] - begin + 1;
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
