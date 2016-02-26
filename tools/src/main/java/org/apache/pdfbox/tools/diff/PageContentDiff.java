package org.apache.pdfbox.tools.diff;

import java.util.Set;

import org.apache.pdfbox.tools.diff.PageContentSet.Coordinate;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;

public class PageContentDiff {

	private DiffSetting setting;
	
	public PageContentDiff(DiffSetting setting) {
		this.setting = setting;
	}

	public PageDiffResult diff(PageContentSet page_1, PageContentSet page_2) {
		PageDiffResult result = new PageDiffResult();

		Set<Coordinate> coSet = page_1.getTextCoordinateSet();
		for (Coordinate co : coSet) {
			TextContent textContent_1 = page_1.getTextContent(co);
			TextContent textContent_2 = page_2.getTextContent(co);
			
			if (!this.diff(textContent_1, textContent_2)) {
				result.append(textContent_1, textContent_2);	
			}
		}

		return result;
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
