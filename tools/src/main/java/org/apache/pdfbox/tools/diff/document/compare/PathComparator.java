package org.apache.pdfbox.tools.diff.document.compare;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageThread.PathSet;

public class PathComparator extends BaseObjectComparator {

	public PathComparator(CompareSetting setting) {
		super(setting);
	}

	public DiffContent[] compare(PathSet basePathSet, PathSet testPathSet) {
		List<DiffContent> result = new ArrayList<DiffContent>();
		
		return result.toArray(new DiffContent[result.size()]);
	}

	@Override
	public DiffContent[] compare(Object base, Object test) {
		return this.compare((PathSet) base, (PathSet) test);
	}
}
