package org.apache.pdfbox.tools.diff.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;

public class GridPage {

	private List<LineThread> lineThreadList;

	public GridPage() {
		this.lineThreadList = new ArrayList<LineThread>();
	}
	
	public void addPageContent(PageContent content) {
		int baseline = content.getY(); 
		int lineHeight = content.getHeight();
		
		LineThread line = this.findLineThread(baseline, lineHeight);
		line.addContentRun(content);
	}
	
	public int lineCount() {
		return this.lineThreadList.size();
	}
	
	public LineThread getLineThread(int line) {
		if (line < 0 || line > this.lineThreadList.size() - 1) {
			return null;
		}
		return this.lineThreadList.get(line);
	}
	
	public List<LineThread> getLineThreadList() {
		List<LineThread> clone = new ArrayList<LineThread>(this.lineThreadList.size());
		clone.addAll(this.lineThreadList);
		return clone;
	}
	
	private LineThread findLineThread(int baseline, int lineHeight) {
		for (LineThread line : this.lineThreadList) {
			if (baseline == line.getBaseline()) {
				return line;
			}
			
			int delta = Math.abs(baseline - line.getBaseline());
			if (delta <= lineHeight / 2.0f) {
				return line;
			}
		}
		
		return this.createLineThread(baseline, lineHeight);	
	}
	
	private LineThread createLineThread(int baseline, int lineHeight) {
		LineThread newLine = new LineThread(baseline, lineHeight);
		
		int size = this.lineThreadList.size();
		if (size == 0 || baseline > this.lineThreadList.get(size - 1).getBaseline()) {
			this.lineThreadList.add(newLine);
			return newLine;
		}
		
		for (int i = 0; i < size; i++) {
			LineThread item = this.lineThreadList.get(i);
			if (baseline < item.getBaseline()) {
				this.lineThreadList.add(i - 1, newLine);
				break;
			}
		}
		
		return newLine;
	}

	public static class LineThread {
		
		private int baseline;
		private int height;
		private List<PageContent> runList;

		public LineThread(int baseline, int height) {
			this.baseline = baseline;
			this.height = height;
			this.runList = new ArrayList<PageContent>();
		}
		
		public int getBaseline() {
			return this.baseline;
		}
		
		public int getLineHeight() {
			return this.height;
		}

		public int runCount() {
			return this.runList.size();
		}
		
		@Override
		public String toString() {
			return this.getLineText();
		}
		
		public String getLineText() {
			StringBuilder buf = new StringBuilder();
			for (PageContent run : this.runList) {
				buf.append(run.showString());
			}
			return buf.toString();
		}
		
		public PageContent getRun(int index) {
			if (index < 0 || index > this.runList.size() - 1) {
				return null;
			}
			return this.runList.get(index);
		}
		
		public void mergeTextRun() {
			
			for (PageContent run : this.runList) {
			}
		}
		
		public List<CharContent> getCharContentList() {
			List<CharContent> ret = new ArrayList<CharContent>();
			
			List<PageContent> textRunList = this.getTextRunList();
			for (PageContent run : textRunList) {
				String text = ((TextContent) run).getText();
				for (int i = 0; i < text.length(); i++) {
					CharContent cc = new CharContent();
					cc.index = i;
					cc.c = text.charAt(i);
					cc.parent = (TextContent) run;
					ret.add(cc);
				}
			}
			return ret;
		}
		
		public static class CharContent {
			public int index;
			public char c;
			public TextContent parent;
		}
		
		public List<PageContent> getTextRunList() {
			return this.getRunList(PageContent.Type.Text);
		}
		
		public List<PageContent> getImageRunList() {
			return this.getRunList(PageContent.Type.Image);
		}
		
		private List<PageContent> getRunList(PageContent.Type type) {
			List<PageContent> list = new ArrayList<PageContent>(0);
			
			for (PageContent run : this.runList) {
				if (run.getType() == type) {
					list.add(run);
				}
			}
			return list;
		}
		
		public void addContentRun(PageContent run) {
			int x = run.getX();
			int size = this.runList.size();
			if (size == 0 || x > this.runList.get(size - 1).getX()) {
				this.runList.add(run);
				return;
			}
			for (int i = 0; i < size; i++) {
				PageContent item = this.runList.get(i);
				if (x < item.getX()) {
					this.runList.add(i - 1, run);
					break;
				}
			}
		}
		
	}

}
