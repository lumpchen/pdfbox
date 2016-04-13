package org.apache.pdfbox.tools.diff.document.compare;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageThread;
import org.apache.pdfbox.tools.diff.document.PageThread.PathLob;
import org.apache.pdfbox.tools.diff.document.PageThread.PathSet;
import org.apache.pdfbox.tools.diff.document.compare.geom.Line2D;
import org.apache.pdfbox.tools.diff.document.compare.geom.Line2D.LineIntersection;
import org.apache.pdfbox.tools.diff.document.compare.geom.Vec2D;

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
		
		Float f1 = pathContent_1 == null ? null : pathContent_1.getGraphicsStateDesc().lineWidth;
		Float f2 = pathContent_2 == null ? null : pathContent_2.getGraphicsStateDesc().lineWidth;
		boolean equals = compare(f1, f2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Line_Width, equals, f1 == null ? null : f1.toString(), 
				f2 == null ? null : f2.toString());
		
		String val_1 = pathContent_1 == null ? null : pathContent_1.getStrokingColorspace();
		String val_2 = pathContent_2 == null ? null : pathContent_2.getStrokingColorspace();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Stroke_Colorspace, equals, val_1, val_2);
		
		val_1 = pathContent_1 == null ? null : pathContent_1.getStrokingColorValue();
		val_2 = pathContent_2 == null ? null : pathContent_2.getStrokingColorValue();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Stroke_Color, equals, val_1, val_2);
		
		val_1 = pathContent_1 == null ? null : pathContent_1.getNonStrokingColorspace();
		val_2 = pathContent_2 == null ? null : pathContent_2.getNonStrokingColorspace();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Fill_Colorspace, equals, val_1, val_2);
		
		val_1 = pathContent_1 == null ? null : pathContent_1.getNonStrokingColorValue();
		val_2 = pathContent_2 == null ? null : pathContent_2.getNonStrokingColorValue();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Fill_Color, equals, val_1, val_2);
		
		return false;
//		return result;
	}

	private PathLob findPathLob(PathLob base, List<PathLob> testPathList) {
		for (PathLob test : testPathList) {
			Rectangle bbox_1 = base.getBBox();
			Rectangle bbox_2 = test.getBBox();
			if (this.comparePath(bbox_1, bbox_2) == 0) {
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
	
	private int comparePath(Rectangle bbox_1, Rectangle bbox_2) {
		if (bbox_1 == null || bbox_2 == null) {
			return -1;
		}
		
		float tolerance = this.setting.toleranceOfPath;

		Line2D line_1 = getMidLine(bbox_1);
		Line2D line_2 = getMidLine(bbox_2);
		
		LineIntersection.Type type = line_1.intersectLine(line_2).getType();
		if (type == LineIntersection.Type.COINCIDENT 
				|| type == LineIntersection.Type.COINCIDENT_NO_INTERSECT) {
			return 0;
		}
		
		if (type == LineIntersection.Type.PARALLEL) {
			float dist = line_1.distanceToPoint(line_2.getMidPoint());
			if (dist <= tolerance) {
				return 0;
			}
		}
		
		if (type == LineIntersection.Type.INTERSECTING) {
			return 1;
		}
		return 1;
		
	}
	
	private static Line2D getMidLine(Rectangle rect) {
		Line2D line = null;
		if (rect.width >= rect.height) { //Hor
			float x1 = rect.x;
			float y1 = rect.y + rect.height / 2.0f;
			float x2 = rect.x + rect.width;
			float y2 = y1;
			
	        Vec2D v1 = new Vec2D(x1, y1);
	        Vec2D v2 = new Vec2D(x2, y2);
	        line = new Line2D(v1, v2);
		} else {
			float x1 = rect.x + rect.width / 2.0f;
			float y1 = rect.y;
			float x2 = rect.x + rect.width / 2.0f;
			float y2 = y1 + rect.height / 2.0f;
			
	        Vec2D v1 = new Vec2D(x1, y1);
	        Vec2D v2 = new Vec2D(x2, y2);
	        line = new Line2D(v1, v2);
		}
		return line;
	}
}
