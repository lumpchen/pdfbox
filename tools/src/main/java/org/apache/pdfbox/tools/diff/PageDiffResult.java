package org.apache.pdfbox.tools.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;

public class PageDiffResult {

	private Set<PageDiffEntry> entrySet;
	
	public PageDiffResult() {
		this.entrySet = new HashSet<PageDiffEntry>();
	}

	public int count() {
		return this.entrySet.size();
	}
	
	public void append(TextContent baseTextContent, TextContent testTextContent) {
		PageDiffEntry entry = new PageDiffEntry();
		entry.category = PageDiffEntry.Category.Text;
		entry.baseContent = baseTextContent;
		entry.testContent = testTextContent;
		this.entrySet.add(entry);
	}

	public List<PageDiffEntry> getDiffContents(PageDiffEntry.Category category) {
		List<PageDiffEntry> ret = new ArrayList<PageDiffEntry>();
		for (PageDiffEntry entry : this.entrySet) {
			if (entry.category == category) {
				ret.add(entry);
			}
		}
		return ret;
	}
	
	public static class PageDiffEntry {
		public static enum Category {
			Text, Image, Path, Annot
		};
		
		public Category category;

		public PageContent baseContent;
		public PageContent testContent;
	}
}
