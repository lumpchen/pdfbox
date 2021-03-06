package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.document.PageContent.AnnotContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.compare.CompareSetting;

public class PageThread {

	private List<PageContent> contentList;
	private TextThread textThread;
	private ImageSet imageSet;
	private PathSet pathSet;
	private AnnotSet annotSet;
	
	CompareSetting setting;

	public PageThread(List<PageContent> contentList, CompareSetting setting) {
		this.setting = setting;
		this.contentList = contentList;
		this.analysis();
	}

	private void analysis() {
		this.textThread = new TextThread();
		this.imageSet = new ImageSet();
		this.pathSet = new PathSet(setting.enableMergePath);
		this.annotSet = new AnnotSet();
		
		if (this.contentList == null || this.contentList.isEmpty()) {
			return;
		}
		for (int i = 0; i < this.contentList.size(); i++) {
			PageContent content = this.contentList.get(i);

			if (content.getType() == PageContent.Type.Text) {
				TextContent textContent = (TextContent) content;
				this.textThread.addTextSpan(textContent);
			} else if (content.getType() == PageContent.Type.Path) {
				PathContent path = (PathContent) content;
				this.pathSet.addPathContent(path);
			} else if (content.getType() == PageContent.Type.Image) {
				ImageContent image = (ImageContent) content;
				this.imageSet.addImageContent(image);
			} else if (content.getType() == PageContent.Type.Annot) {
				AnnotContent annot = (AnnotContent) content;
				this.annotSet.addAnnotContent(annot);
			}
		}
	}
	
	public TextThread getTextThread() {
		return this.textThread;
	}
	
	public ImageSet getImageSet() {
		return this.imageSet;
	}
	
	public PathSet getPathSet() {
		return this.pathSet;
	}
	
	public AnnotSet getAnnotSet() {
		return this.annotSet;
	}
	
	public static class TextThread {
		
		private StringBuilder pageText;
		private List<TextSpan> textSpanList;
		private int nextBegin = 0;
		
		public TextThread() {
			this.pageText = new StringBuilder();
			this.textSpanList = new ArrayList<TextSpan>();
		}
		
		@Override
		public String toString() {
			return this.getText();
		}
		
		public String getText() {
			return this.pageText.toString();
		}
		
		public void addTextSpan(TextContent textContent) {
			String text = textContent.getText() + " ";
			this.pageText.append(text);

			TextSpan span = new TextSpan();
			span.text = text;
			span.begin = this.nextBegin;
			span.length = text.length();
			span.textContent = textContent;
			span.shapeArr = new Shape[span.length];
			List<Shape> shapeList = textContent.getOutlineShapeList();
			if (shapeList != null) {
				for (int i = 0; i < shapeList.size(); i++) {
					span.shapeArr[i] = shapeList.get(i);
				}	
			}
			this.textSpanList.add(span);
			
			nextBegin += text.length();
		}
		
		public int lenToContentEnd(int begin) {
			for (int i = 0; i < this.textSpanList.size(); i++) {
				TextSpan span = this.textSpanList.get(i);
				int[] range = new int[]{span.begin, span.begin + span.length};
				if (begin >= range[0] && begin < range[1]) {
					return range[1] - begin;
				}
			}
			return 0;
		}
		
		public TextLob[] getTextLob(int begin, int length) {
			int end = begin + length;
			int beginContentOffset = 0;
			int beginContentIndex = 0;
			int endContentOffset = 0;
			int endContentIndex = 0;
			
			for (int i = 0; i < this.textSpanList.size(); i++) {
				TextSpan span = this.textSpanList.get(i);
				
				int[] range = new int[] {span.begin, span.begin + span.length};
				if (begin >= range[0] && begin < range[1]) {
					beginContentOffset = begin - range[0];
					beginContentIndex = i;
				}

				if (end > range[0] && end <= range[1]) {
					endContentOffset = end - range[0];
					endContentIndex = i;
					break;
				}
			}
			
			if (beginContentIndex == endContentIndex) {
				StringBuilder buf = new StringBuilder("");
				TextSpan span = this.textSpanList.get(beginContentIndex);
				TextContent run = span.textContent;
				String text = span.text;
				buf.append(text.substring(beginContentOffset, endContentOffset));
				Rectangle2D bbox = span.getBBox(beginContentOffset, endContentOffset);
				return new TextLob[]{new TextLob(buf.toString(), bbox, run)};
			}

			TextLob[] list = new TextLob[endContentIndex - beginContentIndex + 1];
			for (int i = beginContentIndex; i <= endContentIndex; i++) {
				TextSpan span = this.textSpanList.get(i);
				TextContent run = span.textContent;
				if (i == beginContentIndex) {
					String text = span.text.substring(beginContentOffset);
					Rectangle2D rect = span.getBBox(beginContentOffset);
					list[i - beginContentIndex] = new TextLob(text, rect, run);
					continue;
				}
				if (i == endContentIndex) {
					String text = span.text.substring(0, endContentOffset);
					Rectangle2D rect = span.getBBox(0, endContentOffset);
					list[i - beginContentIndex] = new TextLob(text, rect, run);
					continue;
				} else {
					Rectangle2D rect = span.getBBox(0, span.length);
					list[i - beginContentIndex] = new TextLob(run.getText(), rect, run);
				}
			}
			return list;
		}
	}
	
	public static class TextLob {
		private String text;
		private Rectangle2D bBox;
		private TextContent content;

		public TextLob(TextContent content) {
			this.content = content;
			this.text = content.getText();
			this.bBox = content.getOutlineArea().getBounds2D();
		}
		
		public TextLob(String text, Rectangle2D bBox) {
			this.text = text;
			this.bBox = bBox;
		}
		
		public TextLob(String text, Rectangle2D bBox, TextContent content) {
			this(text, bBox);
			this.content = content;
		}


		public String getText() {
			return this.text == null ? "" : this.text;
		}

		public Rectangle2D getBoundingBox() {
			return this.bBox;
		}
		
		public TextContent getContent() {
			return this.content;
		}
		
		public boolean mergeLob(TextLob next) {
			if (next == null) {
				return false;
			}
			
			Rectangle2D rect = next.getBoundingBox();
			double dh = Math.abs(this.getBoundingBox().getY() - rect.getY());
			if (dh <= rect.getHeight()) {
				this.text += next.text;
				this.bBox = this.bBox.createUnion(rect);
				return true;
			}
			return false;
		}
	}
	
	public static class TextSpan {
		String text;
		int begin;
		int length;
		Shape[] shapeArr;
		TextContent textContent;
		
		public Rectangle2D getBBox(int begin) {
			return this.getBBox(begin, this.length);
		}
		
		public Rectangle2D getBBox(int begin, int end) {
			Area area = new Area();
	    	if (this.shapeArr != null) {
	    		for (int i = begin; i < end; i++) {
	    			Shape s = this.shapeArr[i];
	    			if (s == null) {
	    				break;
	    			}
	    			if (s instanceof GeneralPath) {
	    				area.add(new Area(((GeneralPath) s).getBounds2D()));
	    			} else {
	    				area.add(new Area(s));    				
	    			}
	        	}
	    	}
	    	return area.getBounds2D();
		}
	}

	public static class ImageLob {
		
		public int bitsPerComponent;
		public int byteCount;
		public String colorSpace;
		public String decode;
		public int height;
		public int width;
		public String suffix;

		private Rectangle2D bBox;
		
		public ImageLob(ImageContent img) {
			this.bitsPerComponent = img.bitsPerComponent;
			this.byteCount = img.byteCount;
			this.colorSpace = img.colorSpace;
			this.decode = img.decode;
			this.height = img.height;
			this.width = img.width;
			this.suffix = img.suffix;
			
			this.bBox = img.getOutlineArea().getBounds2D();
		}
		
		public Rectangle2D getBBox() {
			return this.bBox;
		}
	}
	
	public static class ImageSet {

		private List<ImageContent> imageList;
		
		public ImageSet() {
			this.imageList = new ArrayList<ImageContent>();
		}
		
		public List<ImageLob> getImageLobList() {
			List<ImageLob> list = new ArrayList<ImageLob>(this.imageList.size());
			for (ImageContent img : this.imageList) {
				list.add(new ImageLob(img));
			}
			return list;
		}
		
		public void addImageContent(ImageContent imageContent) {
			this.imageList.add(imageContent);
		}
		
		public List<ImageContent> getImageList() {
			return this.imageList;
		}
	}
	
	public static class PathLob {
		
		private Rectangle2D bBox;
		private PathContent pathContent;
		
		public PathLob(PathContent pathContent) {
			this.pathContent = pathContent;
			this.bBox = pathContent.getOutlineArea().getBounds2D();
		}
		
		public Rectangle2D getBBox() {
			return this.bBox;
		}
		
		public List<Rectangle2D> getShapeBBox() {
			List<Shape> shapes = this.pathContent.getOutlineShapeList();
			List<Rectangle2D> shapeBBox = new ArrayList<Rectangle2D>(shapes.size());
			for (Shape shape : shapes) {
				shapeBBox.add(shape.getBounds2D());
			}
			return shapeBBox;
		}
		
		public PathContent getPathContent() {
			return this.pathContent;
		}
		
		public String getPaintOperator() {
			return this.getPathContent().isFill() ? "Fill" : "Stroke";
		}
	}
	
	public static class PathSet {
		
		private List<PathContent> pathList;
		private boolean mergePath = false;
		
		public PathSet(boolean mergePath) {
			this.pathList = new ArrayList<PathContent>();
			this.mergePath = mergePath;
		}
		
		private void merge(List<PathContent> mergeList, PathContent content) {
			boolean merged = false;
			for (int i = 0; i < mergeList.size(); i++) {
				if (mergeList.get(i).merge(content)) {
					merged = true;
					break;
				}
			}
			if (!merged) {
				mergeList.add(content);
			}
		}
		
		public void addPathContent(PathContent pathContent) {
			if (this.mergePath) {
				this.merge(this.pathList, pathContent);	
			} else {
				this.pathList.add(pathContent);				
			}
		}
		
		public List<PathLob> getPathLobList() {
			List<PathLob> pathLobList = new ArrayList<PathLob>(this.pathList.size());
			for (PathContent pathContent : this.pathList) {
				pathLobList.add(new PathLob(pathContent));
			}
			
			return pathLobList;
		}
	}
	
	public static class AnnotLob {
		public String subType;
		public String fieldType;
		public String annotName;
		public String annotContents;
		
		private Rectangle bBox;
		private PageContent[] appearance;
		
		public AnnotLob(AnnotContent annot) {
			this.subType = annot.subType;
			this.fieldType = annot.fieldType;
			this.annotName = annot.annotName;
			this.annotContents = annot.annotContents;
			
			this.bBox = annot.getOutlineArea().getBounds();
			this.appearance = annot.getAppearanceContents();
		}
		
		public Rectangle getBBox() {
			return this.bBox;
		}

		public List<PageContent> getAppearance() {
			List<PageContent> contentList = new ArrayList<PageContent>(this.appearance.length);
			for (PageContent content : this.appearance) {
				contentList.add(content);
			}
			return contentList;
		}
	}
	
	public static class AnnotSet {
		
		private List<AnnotContent> annotList;
		
		public AnnotSet() {
			this.annotList = new ArrayList<AnnotContent>();
		}

		public List<AnnotLob> getAnnotLobList() {
			List<AnnotLob> list = new ArrayList<AnnotLob>(this.annotList.size());
			for (AnnotContent content : this.annotList) {
				AnnotLob lob = new AnnotLob(content);
				list.add(lob);
			}
			return list;
		}
		
		public void addAnnotContent(AnnotContent annotContent) {
			this.annotList.add(annotContent);
		}
	}
}

