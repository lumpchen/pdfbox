package org.apache.pdfbox.tools.diff;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageContentSet.Coordinate;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.GridPage;
import org.apache.pdfbox.tools.diff.document.GridPage.LineThread;
import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;

public class GridPageDiffer {

	private DiffSetting setting;

	public GridPageDiffer(DiffSetting setting) {
		this.setting = setting;
	}

	public PageDiffResult diff(GridPage basePage, GridPage testPage) {
		PageDiffResult result = new PageDiffResult();

		int n1 = basePage.lineCount();
		List<LineThread> testLineThreads = testPage.getLineThreadList();

		int runIndex = 0;
		for (int i = 0; i < n1; i++) {
			LineThread baseLineThread = basePage.getLineThread(i);
			int findIndex = this.getLineAsPossible(baseLineThread, i, testLineThreads, runIndex);
			if (findIndex >= 0) {
				runIndex = findIndex;
			}
			LineThread testLineThread = findIndex >= 0 ? testLineThreads.get(findIndex) : null;
			this.diffLineThread(baseLineThread, testLineThread, result);
		}

		return result;
	}

	private void diffLineThread(LineThread baseLineThread, LineThread testLineThread, PageDiffResult result) {

	}
	
	private void diffTextRuns(LineThread baseLineThread, LineThread testLineThread, PageDiffResult result) {
		List<PageContent> baseTextRunList = baseLineThread == null ? null : baseLineThread.getTextRunList();
		List<PageContent> testTextRunList = testLineThread == null ? null : testLineThread.getTextRunList();
		
		if ((baseTextRunList == null || baseTextRunList.isEmpty()) 
				&& (testTextRunList == null || testTextRunList.isEmpty())) {
			return;
		}
		if ((baseTextRunList == null || baseTextRunList.isEmpty()) && testTextRunList != null) {
			DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
			for (PageContent content : testTextRunList) {
				if (!this.diff(null, (TextContent) content, diffContent)) {
					result.append(diffContent);
				}
			}
			return;
		}
		
		if (baseTextRunList != null && (testTextRunList == null || testTextRunList.isEmpty())) {
			DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
			for (PageContent content : baseTextRunList) {
				if (!this.diff((TextContent) content, null, diffContent)) {
					result.append(diffContent);
				}
			}
			return;
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
	
	private int getLineAsPossible(LineThread srcLine, int srcIndex, List<LineThread> lineThreads, int from) {
		if (srcLine == null || lineThreads == null || lineThreads.isEmpty()) {
			return -1;
		}
		int baseline = srcLine.getBaseline();
		String text = srcLine.getLineText();
		
		int indexOfNearest = -1;
		int minNearest = -1;
		int indexOfSimilarest = -1;
		int maxSimilarest = -1;
		for (int i = from; i < lineThreads.size(); i++) {
			LineThread line = lineThreads.get(i);
			int dt = Math.abs(baseline - line.getBaseline());
			int sim = similarity(text, line.getLineText());
			if (i == 0) {
				minNearest = dt;
				maxSimilarest = sim;
			} else {
				if (minNearest > dt) {
					indexOfNearest = i;
					minNearest = dt;
				}
				if (maxSimilarest < sim) {
					indexOfSimilarest = i;
					maxSimilarest = sim;
				}
			}
		}
		if (indexOfNearest == indexOfSimilarest) {
			return indexOfNearest;
		}
		if (maxSimilarest >= 80 && Math.abs(srcIndex - indexOfSimilarest) <= 2) {
			return indexOfSimilarest;
		}
		if (minNearest <= srcLine.getLineHeight() / 2) {
			return indexOfNearest;
		}
		return -1;
	}

	static int similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1.length() < s2.length()) {
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 100;
		}
		float sim = (longerLength - LevenshteinDistance(longer, shorter)) / (float) longerLength;
		return Math.round(sim * 100);
	}

	static int LevenshteinDistance(String s, String t) {
		// degenerate cases
		if (s.equals(t))
			return 0;
		if (s.length() == 0)
			return t.length();
		if (t.length() == 0)
			return s.length();

		// create two work vectors of integer distances
		int[] v0 = new int[t.length() + 1];
		int[] v1 = new int[t.length() + 1];

		// initialize v0 (the previous row of distances)
		// this row is A[0][i]: edit distance for an empty s
		// the distance is just the number of characters to delete from t
		for (int i = 0; i < v0.length; i++)
			v0[i] = i;

		for (int i = 0; i < s.length(); i++) {
			// calculate v1 (current row distances) from the previous row v0

			// first element of v1 is A[i+1][0]
			// edit distance is delete (i+1) chars from s to match empty t
			v1[0] = i + 1;

			// use formula to fill in the rest of the row
			for (int j = 0; j < t.length(); j++) {
				int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
				v1[j + 1] = Math.min(Math.min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost);
			}

			// copy v1 (current row) to v0 (previous row) for next iteration
			for (int j = 0; j < v0.length; j++)
				v0[j] = v1[j];
		}

		return v1[t.length()];
	}

}
