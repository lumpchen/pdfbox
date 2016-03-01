package org.apache.pdfbox.tools.diff;

import java.util.HashSet;
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

	static class PageDiffEntry {
		static enum Category {
			Text, Image, Path, Annot
		};
		
		Category category;

		PageContent baseContent;
		PageContent testContent;
	}
}
