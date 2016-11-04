package org.apache.pdfbox.tools.diff.document.compare;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageThread;
import org.apache.pdfbox.tools.diff.document.PageThread.PathLob;
import org.apache.pdfbox.tools.diff.document.PageThread.PathSet;

public class PathComparator extends ContentComparator {

	public PathComparator(CompareSetting setting) {
		super(setting);
	}

	List<Shape> ass = new ArrayList<Shape>();
	
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

//		show(this.ass);
		
		return result.toArray(new DiffContent[result.size()]);
	}

	private boolean compare(PathLob basePath, PathLob testPath, DiffContent entry) {
		Rectangle2D bbox_1 = basePath == null ? null : basePath.getBBox();
		Rectangle2D bbox_2 = testPath == null ? null : testPath.getBBox();
		entry.setBBox(bbox_1, bbox_2);
		
		List<Rectangle2D> subBbox_1 = basePath == null ? null : basePath.getShapeBBox();
		List<Rectangle2D> subBbox_2 = testPath == null ? null : testPath.getShapeBBox();
		entry.setSubBBox(subBbox_1, subBbox_2);
		
		boolean result = true;
		String val_1, val_2;
		
		val_1 = basePath == null ? null : basePath.getPaintOperator();
		val_2 = testPath == null ? null : testPath.getPaintOperator();
		boolean equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Painting_OP, equals, val_1, val_2);

		PathContent pathContent_1 = basePath == null ? null : basePath.getPathContent();
		PathContent pathContent_2 = testPath == null ? null : testPath.getPathContent();
		
		if ("Fill".equalsIgnoreCase(val_1)) {
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
		} else {
			Float f1 = pathContent_1 == null ? null : pathContent_1.getGraphicsStateDesc().lineWidth;
			Float f2 = pathContent_2 == null ? null : pathContent_2.getGraphicsStateDesc().lineWidth;
			equals = compare(f1, f2);
			result &= equals;
			entry.putAttr(DiffContent.Key.Attr_Line_Width, equals, f1 == null ? null : f1.toString(),
					f2 == null ? null : f2.toString());

			val_1 = pathContent_1 == null ? null : pathContent_1.getStrokingColorspace();
			val_2 = pathContent_2 == null ? null : pathContent_2.getStrokingColorspace();
			equals = compare(val_1, val_2);
			result &= equals;
			entry.putAttr(DiffContent.Key.Attr_Stroke_Colorspace, equals, val_1, val_2);

			val_1 = pathContent_1 == null ? null : pathContent_1.getStrokingColorValue();
			val_2 = pathContent_2 == null ? null : pathContent_2.getStrokingColorValue();
			equals = compare(val_1, val_2);
			result &= equals;
			entry.putAttr(DiffContent.Key.Attr_Stroke_Color, equals, val_1, val_2);
		}

		if (bbox_1 != null) {
			equals = bbox_1.equals(bbox_2);
		} else {
			equals = false;
		}
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Frame_size, equals, asString(bbox_1), asString(bbox_2));
		
		return result;
	}

	private PathLob findPathLob(PathLob base, List<PathLob> testPathList) {
		for (PathLob test : testPathList) {
			PathContent path_1 = base.getPathContent();
			PathContent path_2 = test.getPathContent();
			if (this.comparePathContent(path_1, path_2)) {
				return test;
			}
		}
		return null;
	}

	private boolean comparePathContent(PathContent path_1, PathContent path_2) {
		if (path_1.isFill() != path_2.isFill()) {
			return false;
		}
		
//		this.lookGeneralPath(path_1);
		
//		this.lookGeneralPath(path_2);
//		List<Shape> shapeList_1 = path_1.getOutlineShapeList();
//		Area area_1 = this.toArea(shapeList_1);
//		
//		List<Shape> shapeList_2 = path_2.getOutlineShapeList();
//		Area area_2 = this.toArea(shapeList_2);
		
		Area area_1 = path_1.getOutlineArea();
		Area area_2 = path_2.getOutlineArea();
		if (this.equalsWithTolerance(area_1, area_2)) {
			return true;
		}
		
		return false;
	}

	private Area toArea(List<Shape> shapeList) {
		Area area = new Area();
		for (Shape s : shapeList) {
			area.add(new Area(s.getBounds2D()));
		}
		return area;
	}
	
	private boolean equalsWithTolerance(Area a1, Area a2) {
		Rectangle r1 = a1.getBounds();
		Rectangle r2 = a2.getBounds();
		if (r1.width != r2.width || r1.height != r2.height) {
			return false;
		}
		float tor = this.setting.toleranceOfPath;
		int dx = r1.x - r2.x;
		int dy = r1.y - r2.y;
		if (Math.abs(dx) <= tor && Math.abs(dy) <= tor) {
			AffineTransform at = new AffineTransform(1, 0, 0, 1, dx, dy);
			Area ta2 = a2.createTransformedArea(at);
			if (a1.equals(ta2)) {
				return true;
			}
		}
		return false;
	}
	
	private void lookGeneralPath(PathContent content) {
		List<Shape> shapes = content.getOutlineShapeList();
		
		this.ass.addAll(shapes);
		
		
		for (int i = 0; i < shapes.size(); i++) {
			GeneralPath p = (GeneralPath) shapes.get(i);
			PathIterator iter = p.getPathIterator(null);
			float[] coords = new float[6];
			
			while (!iter.isDone()) {
				switch (iter.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					System.out.println("moveto");
					break;
				case PathIterator.SEG_LINETO:
					System.out.println("lineto");
					break;
				case PathIterator.SEG_CUBICTO:
					System.out.println("cubicto");
					break;
				case PathIterator.SEG_CLOSE:
					System.out.println("close");
					break;
				case PathIterator.SEG_QUADTO:
					System.out.println("quadto");
					break;
				}
				iter.next();
			}
		}
	}
	
	static void show(List<Shape> shapes ) {
		try {
			BufferedImage img = ImageIO.read(new File("c:/temp/1.png"));
			Graphics2D g = (Graphics2D) img.createGraphics();
			
			float dash1[] = {10.0f};
			BasicStroke dashed = new BasicStroke(1.0f);
			g.setStroke(dashed);
			
			for (int i = 0; i < shapes.size(); i++) {
				GeneralPath p = (GeneralPath) shapes.get(i);
				g.setColor(Color.red);
				g.draw(p);
				g.setColor(Color.black);
//				g.fill(p);
			}
			
			ImageIO.write(img, "png", new File("c:/temp/1-1.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public DiffContent[] compare(PageThread basePageThread, PageThread testPageThread) {
		DiffContent[] diffs = this.compare(basePageThread.getPathSet(), testPageThread.getPathSet());
		return diffs;
	}

}
