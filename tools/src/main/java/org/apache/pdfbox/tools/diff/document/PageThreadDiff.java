package org.apache.pdfbox.tools.diff.document;

import java.awt.geom.Area;
import java.util.LinkedList;

import org.apache.pdfbox.tools.diff.PageDiffResult;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;
import org.apache.pdfbox.tools.diff.document.PageThread.TextLob;
import org.apache.pdfbox.tools.diff.document.PageThread.TextThread;
import org.apache.pdfbox.tools.diff.name.fraser.neil.plaintext.diff_match_patch.Diff;
import org.apache.pdfbox.tools.diff.name.fraser.neil.plaintext.diff_match_patch.Operation;

public class PageThreadDiff {

	public PageThreadDiff() {
	}

	public PageDiffResult diff(PageThread basePage, PageThread testPage) {
		PageDiffResult result = new PageDiffResult();
		this.diffText(basePage.getTextThread(), testPage.getTextThread(), result);
		
		return result;
	}

	private void diffText(TextThread baseTextThread, TextThread testTextThread, PageDiffResult result) {
		String baseText = baseTextThread.getText();
		String testText = testTextThread.getText();
		LinkedList<Diff> diffs = TextDiffUtil.diffText(baseText, testText);
		if (diffs == null || diffs.isEmpty()) {
			return;
		}

		int ibase = 0, itest = 0;
		for (Diff diff : diffs) {
			if (diff.operation == Operation.INSERT) {
				int from = itest;
				itest += diff.text.length();
				TextLob[] lobs = testTextThread.getTextLob(from, itest);
				
				for (TextLob lob : lobs) {
					DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
					diffContent.putAttr(DiffContent.Key.Attr_Text, false, "#INSERT#", diff.text);
					diffContent.setBBox(null, lob.getBoundingBox());
					result.append(diffContent);					
				}
			} else if (diff.operation == Operation.DELETE) {
				int from = ibase;
				ibase += diff.text.length();
				TextLob[] lobs = baseTextThread.getTextLob(from, ibase);
				for (TextLob lob : lobs) {
					DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
					diffContent.putAttr(DiffContent.Key.Attr_Text, false, diff.text, "#DELETE#");
					diffContent.setBBox(lob.getBoundingBox(), null);
					result.append(diffContent);					
				}
			} else {
				int baseBegin = 0;
				int testBegin = 0;
				int walk = 0;
				while (true) {
					if (walk >= diff.text.length()) {
						ibase += diff.text.length();
						itest += diff.text.length();
						break;
					}
					int baseLen = baseTextThread.lenToContentEnd(baseBegin + ibase);
					int testLen = testTextThread.lenToContentEnd(testBegin + itest);
					
					int slot = baseLen <= testLen ? baseLen : testLen;
					if (slot > diff.text.length() - baseBegin) {
						slot = diff.text.length() - baseBegin;
					}
					String text = diff.text.substring(baseBegin, baseBegin + slot);
					TextLob baseLob = baseTextThread.getTextLob(baseBegin, baseBegin + slot - 1)[0];
					TextLob testLob = testTextThread.getTextLob(testBegin, testBegin + slot - 1)[0];
					DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
					diffContent.putAttr(DiffContent.Key.Attr_Text, true, text, text);
					if (!this.diff(baseLob.getContent(), testLob.getContent(), diffContent)) {
						diffContent.setBBox(baseLob.getBoundingBox(), testLob.getBoundingBox());
						result.append(diffContent);
					}
					baseBegin += slot;
					testBegin += slot;
					walk += slot;
				}
			}
		}
	}
	
	private boolean diff(TextContent textContent_1, TextContent textContent_2, DiffContent entry) {
		boolean result = true;
		
		String val_1 = textContent_1 == null ? null : textContent_1.getFontName();
		String val_2 = textContent_2 == null ? null : textContent_2.getFontName();
		boolean equals = diff(removeFontNameSuffix(val_1), removeFontNameSuffix(val_2));
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Font, equals, val_1, val_2);
		
		Float size_1 = textContent_1 == null ? null : textContent_1.getFontSize();
		Float size_2 = textContent_2 == null ? null : textContent_2.getFontSize();
		equals = diff(size_1, size_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Font_size, equals, size_1 == null ? null : size_1.toString(), 
				size_2 == null ? null : size_2.toString());
		
		val_1 = textContent_1 == null ? null : textContent_1.getNonStrokingColorspace();
		val_2 = textContent_2 == null ? null : textContent_2.getNonStrokingColorspace();
		equals = diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Colorspace, equals, val_1, val_2);
		
		return result;
	}
	
	private static String removeFontNameSuffix(String fontName) {
		if (fontName == null) {
			return null;
		}
		if (fontName.indexOf("+") > 0) {
			return fontName.substring(fontName.indexOf("+"), fontName.length() - 1);
		} 
		return fontName;
	}
	
	private static boolean diff(Float f1, Float f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return f1 - f2 <= 0.00001;
	}
	
	private static boolean diff(Integer f1, Integer f2) {
		if (f1 == null || f2 == null) {
			return false;
		}
		return f1.intValue() == f2.intValue();
	}
	
	private static boolean diff(GraphicsStateDesc gstate_1, GraphicsStateDesc gstate_2) {
		if (gstate_1 == null && gstate_2 == null) {
			return true;
		}
		if (gstate_1 != null && gstate_2 != null) {
			boolean b = diff(gstate_1.textState, gstate_2.textState);
			b &= diff(gstate_1.nonStrokingColor, gstate_2.nonStrokingColor);
			b &= diff(gstate_1.strokingColor, gstate_2.strokingColor);
			return b;
		}
		return false;
	}
	
	private static boolean diff(TextStateDesc tstate_1, TextStateDesc tstate_2) {
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
	
	private static boolean diff(ColorDesc color_1, ColorDesc color_2) {
		if (color_1 == null && color_2 == null) {
			return true;
		}
		if (color_1 != null && color_2 != null) {
			boolean b = diff(color_1.patternName, color_2.patternName);
			b &= diff(color_1.colorSpace, color_2.colorSpace);
			b &= diff(color_1.components, color_2.components);
		    return b;	
		}
		return false;
	}
	
	private static boolean diff(float[] arr1, float[] arr2) {
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
	
	private static boolean diff(String s1, String s2) {
		if (s1 != null) {
			return s1.equals(s2);
		}
		if (s2 != null) {
			return s2.equals(s1);
		}
		return true;
	}
}
