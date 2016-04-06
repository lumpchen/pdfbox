package org.apache.pdfbox.tools.diff.document.compare;import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageThread;

public class PageContentComparator {
	
	private TextComparator textComparator;
	private ImageComparator imageComparator;
	private PathComparator pathComparator;
	private AnnotComparator annotComparator;
	
	public PageContentComparator() {
		this(null);
	}
	
	public PageContentComparator(CompareSetting setting) {
		this.textComparator = new TextComparator(setting);
		this.imageComparator = new ImageComparator(setting);
		this.pathComparator = new PathComparator(setting);
		this.annotComparator = new AnnotComparator(setting);
	}
	
	public PageDiffResult compare(List<PageContent> basePageContents, List<PageContent> testPageContents) {
		PageThread basePageThread = new PageThread(basePageContents);
		PageThread testPageThread = new PageThread(testPageContents);
		
		PageDiffResult result = new PageDiffResult();
		DiffContent[] diffs = this.textComparator.compare(basePageThread.getTextThread(), testPageThread.getTextThread());
		result.append(diffs);
		
		diffs = this.imageComparator.compare(basePageThread.getImageSet(), testPageThread.getImageSet());
		result.append(diffs);
		
		diffs = this.pathComparator.compare(basePageThread.getPathSet(), testPageThread.getPathSet());
		result.append(diffs);

		diffs = this.annotComparator.compare(basePageThread.getAnnotSet(), testPageThread.getAnnotSet());
		result.append(diffs);
		
		return result;
	}
}
