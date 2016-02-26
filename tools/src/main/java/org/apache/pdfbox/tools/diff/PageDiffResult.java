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
	
	public void append(TextContent textContent_1, TextContent textContent_2) {
		PageDiffEntry entry = new PageDiffEntry();
		entry.category = PageDiffEntry.Category.Text;
		entry.content_1 = textContent_1;
		entry.content_2 = textContent_2;
		this.entrySet.add(entry);
	}

	static class PageDiffEntry {
		static enum Category {
			Text, Image, Path, Annot
		};
		
		Category category;

		PageContent content_1;
		PageContent content_2;
	}
}
