package org.apache.pdfbox.tools.diff.document.compare;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageThread;
import org.apache.pdfbox.tools.diff.document.PageThread.PathLob;
import org.apache.pdfbox.tools.diff.document.PageThread.PathSet;

public class PathComparator extends ContentComparator {

	public PathComparator(CompareSetting setting) {
		super(setting);
	}

	public DiffContent[] compare(PathSet basePathSet, PathSet testPathSet) {
		List<PathLob> basePathList = basePathSet.getPathLobList();
		List<PathLob> testPathList = testPathSet.getPathLobList();
		List<DiffContent> result = new ArrayList<DiffContent>();
		
		for (int i = 0; i < basePathList.size(); i++) {
			PathLob basePath = basePathList.get(i);
			PathLob testPath = this.findPathLob(basePath, testPathList);
	
			DiffContent diffContent = new DiffContent(DiffContent.Category.Path);
			if (!this.compare(basePath, testPath, diffContent)) {
				result.add(diffContent);
			}
			if (testPath != null) {
				testPathList.remove(testPath);
			}
		}
		
		// process remain path in test
		for (PathLob path : testPathList) {
			DiffContent diffContent = new DiffContent(DiffContent.Category.Path);
			if (!this.compare(null, path, diffContent)) {
				result.add(diffContent);
			}
		}
		
		return result.toArray(new DiffContent[result.size()]);
	}
	
	private boolean compare(PathLob basePath, PathLob testPath, DiffContent entry) {
		Rectangle bbox_1 = basePath == null ? null : basePath.getBBox();
		Rectangle bbox_2 = testPath == null ? null : testPath.getBBox();
		entry.setBBox(bbox_1, bbox_2);
		boolean result = true;
		
		PathContent pathContent_1 = basePath == null ? null : basePath.getPathContent();
		PathContent pathContent_2 = testPath == null ? null : testPath.getPathContent();
		
		String val_1 = pathContent_1 == null ? null : pathContent_1.getStrokingColorspace();
		String val_2 = pathContent_2 == null ? null : pathContent_2.getStrokingColorspace();
		boolean equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Colorspace, equals, val_1, val_2);
		
		val_1 = pathContent_1 == null ? null : pathContent_1.getStrokingColorValue();
		val_2 = pathContent_2 == null ? null : pathContent_2.getStrokingColorValue();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Color, equals, val_1, val_2);
		return result;
	}

	private PathLob findPathLob(PathLob base, List<PathLob> testPathList) {
		for (PathLob test : testPathList) {
			if (base.getBBox().intersects(test.getBBox())) {
				return test;
			}
		}
		return null;
	}

	@Override
	public DiffContent[] compare(PageThread basePageThread, PageThread testPageThread) {
		DiffContent[] diffs = this.compare(basePageThread.getPathSet(), testPageThread.getPathSet());
		return diffs;
	}
	
}
