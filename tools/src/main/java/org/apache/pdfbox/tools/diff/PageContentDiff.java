package org.apache.pdfbox.tools.diff;

import java.awt.geom.Area;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.tools.diff.PageContentSet.Coordinate;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;

public class PageContentDiff {

	private DiffSetting setting;
	
	public PageContentDiff(DiffSetting setting) {
		this.setting = setting;
	}

	public PageDiffResult diff(PageContentSet page_1, PageContentSet page_2) {
		PageDiffResult result = new PageDiffResult();

		this.diffText(page_1, page_2, result);
		this.diffImage(page_1, page_2, result);
		this.diffPath(page_1, page_2, result);
		return result;
	}
	
	private void diffText_1(List<TextContent> textLine_1, List<TextContent> textLine_2, PageDiffResult result) {
		if (textLine_1.size() == textLine_2.size()) {
			for (int i = 0; i < textLine_1.size(); i++) {
				DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
				if (!this.diff(textLine_1.get(i), textLine_2.get(i), diffContent)) {
					result.append(diffContent);
				}
			}
			return;
		}
		
		int n1 = textLine_1.size();
		int n2 = textLine_2.size();
		int n = n1 > n2 ? n1 : n2;
		for (int i = 0; i < n; i++) {
			TextContent textContent_1 = this.nullGet(textLine_1, i);
			TextContent textContent_2 = this.nullGet(textLine_2, i);
			DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
			if (!this.diff(textContent_1, textContent_2, diffContent)) {
				result.append(diffContent);
			}
		}
	}
	
	private TextContent nullGet(List<TextContent> list, int index) {
		if (index < 0 || index >= list.size()) {
			return null;
		}
		return list.get(index);
	}
	
	private void diffText_1(PageContentSet page_1, PageContentSet page_2, PageDiffResult result) {
		int lineCount_1 = page_1.countTextLine();
		int lineCount_2 = page_2.countTextLine();
		int lineCount = lineCount_1 >= lineCount_2 ? lineCount_1 : lineCount_2;
		for (int i = 0; i < lineCount; i++) {
			List<TextContent> textLine_1 = page_1.getTextLine(i);
			List<TextContent> textLine_2 = page_2.getTextLine(i);
			this.diffText_1(textLine_1, textLine_2, result);
		}
	}
	
	private void diffText(PageContentSet page_1, PageContentSet page_2, PageDiffResult result) {
		Set<Coordinate> coSet_1 = page_1.getTextCoordinateSet();
		Set<Coordinate> coSet_2 = page_2.getTextCoordinateSet();

		Set<Coordinate> sharedContent = new HashSet<Coordinate>();
		for (Coordinate co : coSet_1) {
			TextContent textContent_1 = page_1.getTextContent(co);
			TextContent textContent_2 = page_2.getTextContent(co);

			if (textContent_1 == null || textContent_2 == null) {
				continue;
			}
			DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
			if (!this.diff(textContent_1, textContent_2, diffContent)) {
				result.append(diffContent);
			}
			sharedContent.add(co);
		}

		coSet_1.removeAll(sharedContent);
		coSet_2.removeAll(sharedContent);
		sharedContent.clear();
		
		if (!coSet_1.isEmpty()) {
			for (Coordinate co : coSet_1) {
				Coordinate find = findNearestContent(co, coSet_2);
				TextContent textContent_1 = page_1.getTextContent(co);
				TextContent textContent_2 = find != null ? page_2.getTextContent(find) : null;

				DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
				this.diff(textContent_1, textContent_2, diffContent);
				result.append(diffContent);
				sharedContent.add(co);
				if (find != null) {
					sharedContent.add(find);	
				}
			}
		}
		coSet_1.removeAll(sharedContent);
		coSet_2.removeAll(sharedContent);
		
		if (!coSet_2.isEmpty()) {
			// not found text content in base
			for (Coordinate co : coSet_2) {
				TextContent textContent_2 = page_2.getTextContent(co);
				DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
				if (!this.diff(null, textContent_2, diffContent)) {
					result.append(diffContent);
				}
			}
		}
	}
	
	public static final int DELTA = 6;
	private Coordinate findNearestContent(Coordinate coords, Set<Coordinate> coSet) {
		if (coSet == null || coSet.isEmpty()) {
			return null;
		}
		if (coSet.contains(coords)) {
			return coords;
		}
		for (Coordinate co : coSet) {
			int dx = Math.abs(co.x - coords.x);
			int dy = Math.abs(co.y - coords.y);
			
			if (dx <= DELTA && dy <= DELTA) {
				return co;
			}
		}
		return null;
	}
	
	private boolean diff(TextContent textContent_1, TextContent textContent_2, DiffContent entry) {
		Area outline_1 = textContent_1 == null ? null : textContent_1.getOutlineArea();
		Area outline_2 = textContent_2 == null ? null : textContent_2.getOutlineArea();
		entry.setOutline(outline_1, outline_2);
		boolean result = true;
		String val_1 = textContent_1 == null ? null : textContent_1.getText();
		String val_2 = textContent_2 == null ? null : textContent_2.getText();
		boolean equals = this.diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Text, equals, val_1, val_2);
		
		val_1 = textContent_1 == null ? null : textContent_1.getFontName();
		val_2 = textContent_2 == null ? null : textContent_2.getFontName();
		equals = this.diff(removeFontNameSuffix(val_1), removeFontNameSuffix(val_2));
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Font, equals, val_1, val_2);
		
		Float size_1 = textContent_1 == null ? null : textContent_1.getFontSize();
		Float size_2 = textContent_2 == null ? null : textContent_2.getFontSize();
		equals = this.diff(size_1, size_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Font_size, equals, size_1 == null ? null : size_1.toString(), 
				size_2 == null ? null : size_2.toString());
		
		val_1 = textContent_1 == null ? null : textContent_1.getNonStrokingColorspace();
		val_2 = textContent_2 == null ? null : textContent_2.getNonStrokingColorspace();
		equals = this.diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Colorspace, equals, val_1, val_2);
		
		return result;
	}
	
	private String removeFontNameSuffix(String fontName) {
		if (fontName == null) {
			return null;
		}
		if (fontName.indexOf("+") > 0) {
			return fontName.substring(fontName.indexOf("+"), fontName.length() - 1);
		} 
		return fontName;
	}
	
	private boolean diff(Float f1, Float f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return f1 - f2 <= 0.00001;
	}
	
	private boolean diff(Integer f1, Integer f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return f1.intValue() == f2.intValue();
	}
	
	private void diffImage(PageContentSet page_1, PageContentSet page_2, PageDiffResult result) {
		Set<Coordinate> coSet_1 = page_1.getImageCoordinateSet();
		Set<Coordinate> coSet_2 = page_2.getImageCoordinateSet();
		Set<Coordinate> sharedContent = new HashSet<Coordinate>();
		
		for (Coordinate co : coSet_1) {
			ImageContent imageContent_1 = page_1.getImageContent(co);
			ImageContent imageContent_2 = page_2.getImageContent(co);
		
			if (imageContent_1 == null || imageContent_2 == null) {
				continue;
			}
			
			DiffContent diffContent = new DiffContent(DiffContent.Category.Image);
			if (!this.diff(imageContent_1, imageContent_2, diffContent)) {
				result.append(diffContent);
			}
			sharedContent.add(co);
		}
		
		coSet_1.removeAll(sharedContent);
		coSet_2.removeAll(sharedContent);
		sharedContent.clear();
		
		if (!coSet_1.isEmpty()) {
			for (Coordinate co : coSet_1) {
				Coordinate find = findNearestContent(co, coSet_2);
				ImageContent imageContent_1 = page_1.getImageContent(co);
				ImageContent imageContent_2 = find != null ? page_2.getImageContent(find) : null;

				DiffContent diffContent = new DiffContent(DiffContent.Category.Image);
				this.diff(imageContent_1, imageContent_2, diffContent);
				result.append(diffContent);
				sharedContent.add(co);
				if (find != null) {
					sharedContent.add(find);
				}
			}
		}
		coSet_1.removeAll(sharedContent);
		coSet_2.removeAll(sharedContent);
		
		if (!coSet_2.isEmpty()) {
			// not found text content in base
			for (Coordinate co : coSet_2) {
				ImageContent imageContent_2 = page_2.getImageContent(co);
				DiffContent diffContent = new DiffContent(DiffContent.Category.Image);
				if (!this.diff(null, imageContent_2, diffContent)) {
					result.append(diffContent);
				}
			}
		}
	}
	
	private boolean diff(ImageContent imageContent_1, ImageContent imageContent_2, DiffContent entry) {
		Area outline_1 = imageContent_1 == null ? null : imageContent_1.getOutlineArea();
		Area outline_2 = imageContent_2 == null ? null : imageContent_2.getOutlineArea();
		entry.setOutline(outline_1, outline_2);
		boolean result = true;
		
		Integer val_1 = imageContent_1 == null ? null : imageContent_1.width;
		Integer val_2 = imageContent_2 == null ? null : imageContent_2.width;
		boolean equals = this.diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Width, equals, val_1, val_2);
		
		val_1 = imageContent_1 == null ? null : imageContent_1.height;
		val_2 = imageContent_2 == null ? null : imageContent_2.height;
		equals = this.diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Height, equals, val_1, val_2);
		
		val_1 = imageContent_1 == null ? null : imageContent_1.byteCount;
		val_2 = imageContent_2 == null ? null : imageContent_2.byteCount;
		equals = this.diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Byte_count, equals, val_1, val_2);
		
		val_1 = imageContent_1 == null ? null : imageContent_1.bitsPerComponent;
		val_2 = imageContent_2 == null ? null : imageContent_2.bitsPerComponent;
		equals = this.diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Bits_Per_Component, equals, val_1, val_2);
		
		return result;
	}
	
	private void diffPath(PageContentSet page_1, PageContentSet page_2, PageDiffResult result) {
		
	}

	private boolean diff(TextContent textContent_1, TextContent textContent_2) {
		if (textContent_1 == null || textContent_2 == null) {
			return false;
		}

		if (!this.diff(textContent_1.getText(), textContent_2.getText())) {
			return false;
		}
		if (!this.diff(textContent_1.getGraphicsStateDesc(), textContent_2.getGraphicsStateDesc())) {
			return false;
		}
		
		return true;
	}

	private boolean diff(GraphicsStateDesc gstate_1, GraphicsStateDesc gstate_2) {
		if (gstate_1 == null && gstate_2 == null) {
			return true;
		}
		if (gstate_1 != null && gstate_2 != null) {
			boolean b = this.diff(gstate_1.textState, gstate_2.textState);
			b &= this.diff(gstate_1.nonStrokingColor, gstate_2.nonStrokingColor);
			b &= this.diff(gstate_1.strokingColor, gstate_2.strokingColor);
			return b;
		}
		return false;
	}
	
	private boolean diff(TextStateDesc tstate_1, TextStateDesc tstate_2) {
		if (tstate_1 == null && tstate_2 == null) {
			return true;
		}
		if (tstate_1 != null && tstate_2 != null) {
			boolean b = tstate_1.characterSpacing == tstate_2.characterSpacing
					&& tstate_1.wordSpacing == tstate_2.wordSpacing
					&& tstate_1.horizontalScaling == tstate_2.horizontalScaling 
					&& tstate_1.leading == tstate_2.leading
					&& tstate_1.fontSize == tstate_2.fontSize 
					&& tstate_1.renderingMode == tstate_2.renderingMode
					&& tstate_1.rise == tstate_2.rise 
					&& tstate_1.knockout == tstate_2.knockout;
			b &= diff(tstate_1.fontName, tstate_2.fontName);
			return b;	
		}
		return false;
	}
	
	private boolean diff(ColorDesc color_1, ColorDesc color_2) {
		if (color_1 == null && color_2 == null) {
			return true;
		}
		if (color_1 != null && color_2 != null) {
			boolean b = this.diff(color_1.patternName, color_2.patternName);
			b &= this.diff(color_1.colorSpace, color_2.colorSpace);
			b &= this.diff(color_1.components, color_2.components);
		    return b;	
		}
		return false;
	}
	
	private boolean diff(float[] arr1, float[] arr2) {
		if (arr1 == null && arr2 == null) {
			return true;
		}
		if (arr1 != null && arr2 != null) {
			if (arr1.length != arr2.length) {
				return false;
			}
			for (int i = 0; i < arr1.length; i++) {
				if (arr1[i] != arr2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean diff(String s1, String s2) {
		if (s1 != null) {
			return s1.equals(s2);
		}
		if (s2 != null) {
			return s2.equals(s1);
		}
		return true;
	}
}
