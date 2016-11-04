package org.apache.pdfbox.tools.diff.document.compare;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageThread;

public class PageContentComparator {

	private CompareSetting setting;
	private List<ContentComparator> comparatorQueue;

	public PageContentComparator() {
		this(new CompareSetting());
	}

	public PageContentComparator(CompareSetting setting) {
		this.setting = setting;
		this.comparatorQueue = new ArrayList<ContentComparator>(4);
		
		this.comparatorQueue.add(new TextComparator(setting));
		this.comparatorQueue.add(new ImageComparator(setting));

		if (this.setting.enableComparePath) {
			this.comparatorQueue.add(new PathComparator(setting));
		}

		if (this.setting.enableCompareAnnots) {
			this.comparatorQueue.add(new AnnotComparator(setting));
		}
	}

	public PageDiffResult compare(List<PageContent> basePageContents, List<PageContent> testPageContents) {
		PageThread basePageThread = new PageThread(basePageContents, this.setting);
		PageThread testPageThread = new PageThread(testPageContents, this.setting);

		PageDiffResult result = new PageDiffResult();

		for (ContentComparator comparator : this.comparatorQueue) {
			DiffContent[] diffs = comparator.compare(basePageThread, testPageThread);
			result.append(diffs);
		}

		return result;
	}

}
