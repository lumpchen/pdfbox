package org.apache.pdfbox.tools.diff.document.compare;

import java.awt.geom.Rectangle2D;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;
import org.apache.pdfbox.tools.diff.document.PageThread;

public abstract class ContentComparator {
	
	CompareSetting setting;
	
	public ContentComparator(CompareSetting setting) {
		this.setting = setting;
	}
	
	public abstract DiffContent[] compare(PageThread basePageThread, PageThread testPageThread);
	
	protected boolean compare(Float f1, Float f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return Math.abs(f1 - f2) <= 0.00001;
	}
	
	protected boolean compare(Integer f1, Integer f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return f1.intValue() == f2.intValue();
	}
	
	protected boolean compare(GraphicsStateDesc gstate_1, GraphicsStateDesc gstate_2) {
		if (gstate_1 == null && gstate_2 == null) {
			return true;
		}
		if (gstate_1 != null && gstate_2 != null) {
			boolean b = compare(gstate_1.textState, gstate_2.textState);
			b &= compare(gstate_1.nonStrokingColor, gstate_2.nonStrokingColor);
			b &= compare(gstate_1.strokingColor, gstate_2.strokingColor);
			return b;
		}
		return false;
	}
	
	protected boolean compare(TextStateDesc tstate_1, TextStateDesc tstate_2) {
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
			b &= compare(tstate_1.fontName, tstate_2.fontName);
			return b;	
		}
		return false;
	}
	
	protected boolean compare(ColorDesc color_1, ColorDesc color_2) {
		if (color_1 == null && color_2 == null) {
			return true;
		}
		if (color_1 != null && color_2 != null) {
			boolean b = compare(color_1.patternName, color_2.patternName);
			b &= compare(color_1.colorSpace, color_2.colorSpace);
			b &= compare(color_1.components, color_2.components);
		    return b;	
		}
		return false;
	}
	
	protected boolean compare(float[] arr1, float[] arr2) {
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
	
	protected boolean compare(String s1, String s2) {
		if (s1 != null) {
			return s1.equals(s2);
		}
		if (s2 != null) {
			return s2.equals(s1);
		}
		return true;
	}
	
	protected String asString(Rectangle2D rect) {
		if (rect == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("x=" + rect.getX());
		buf.append(", ");
		buf.append("y=" + rect.getY());
		buf.append(", ");
		buf.append("width=" + rect.getWidth());
		buf.append(", ");
		buf.append("height=" + rect.getHeight());
		return buf.toString();
	}
	
	protected String removeFontNameSuffix(String fontName) {
		if (fontName == null) {
			return null;
		}
		if (fontName.indexOf("+") > 0) {
			return fontName.substring(fontName.indexOf("+"), fontName.length() - 1);
		} 
		return fontName;
	}
}
