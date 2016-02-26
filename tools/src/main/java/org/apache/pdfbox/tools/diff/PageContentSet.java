package org.apache.pdfbox.tools.diff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.AnnotContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;

public class PageContentSet {

	private int pageNo;
	private List<PageContent> contentList;

	private Map<Coordinate, TextContent> textContentSet;
	private Map<Coordinate, PathContent> pathContentSet;
	private Map<Coordinate, ImageContent> imageContentSet;
	private Map<Coordinate, AnnotContent> annotContentSet;
	
	public PageContentSet(int pageNo, List<PageContent> contentList) {
		this.pageNo = pageNo;
		this.contentList = contentList;
		
		this.textContentSet = new HashMap<Coordinate, TextContent>();
		this.pathContentSet = new HashMap<Coordinate, PathContent>();
		this.imageContentSet = new HashMap<Coordinate, ImageContent>();
		this.annotContentSet = new HashMap<Coordinate, AnnotContent>(0);
		
		this.category();
	}

	public int getPageNo() {
		return this.pageNo;
	}
	
	private void category() {
		if (this.contentList.isEmpty()) {
			return;
		}

		for (int i = 0; i < this.contentList.size(); i++) {
			PageContent content = this.contentList.get(i);
			int x = content.getOutlineArea().getBounds().x;
			int y = content.getOutlineArea().getBounds().y;

			Coordinate co = new Coordinate(x, y);
			
			if (content.getType() == PageContent.Type.Text) {
				TextContent text = (TextContent) content;
				this.textContentSet.put(co, text);
			} else if (content.getType() == PageContent.Type.Path) {
				PathContent path = (PathContent) content;
				this.pathContentSet.put(co, path);
			} else if (content.getType() == PageContent.Type.Image) {
				ImageContent image = (ImageContent) content;
				this.imageContentSet.put(co, image);
			} else if (content.getType() == PageContent.Type.Annot) {
				AnnotContent annot = (AnnotContent) content;
				this.annotContentSet.put(co, annot);
			}
		}
	}
	
	public Set<Coordinate> getTextCoordinateSet() {
		return this.textContentSet.keySet();
	}
	
	public TextContent getTextContent(Coordinate co) {
		return this.textContentSet.get(co);
	}

	static class Coordinate {
		int x;
		int y;

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			return this.x * 31 + this.y;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Coordinate)) {
				return false;
			}
			return this.x == ((Coordinate) obj).x && this.y == ((Coordinate) obj).y;
		}
	}
}
