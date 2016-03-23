package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ColorDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.GraphicsStateDesc;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextStateDesc;
import org.apache.pdfbox.tools.diff.document.PageThread.AnnotThread;
import org.apache.pdfbox.tools.diff.document.PageThread.ImageLob;
import org.apache.pdfbox.tools.diff.document.PageThread.ImageThread;
import org.apache.pdfbox.tools.diff.document.PageThread.PathThread;
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
		this.diffImage(basePage.getImageThread(), testPage.getImageThread(), result);
		this.diffPath(basePage.getPathThread(), testPage.getPathThread(), result);
		this.diffAnnot(basePage.getAnnotThread(), testPage.getAnnotThread(), result);
		
		return result;
	}

	private void diffAnnot(AnnotThread baseAnnotThread, AnnotThread testAnnotThread, PageDiffResult result) {
	}
	
	private void diffPath(PathThread basePathThread, PathThread testPathThread, PageDiffResult result) {
	}
	
	private void diffImage(ImageThread baseImageThread, ImageThread testImageThread, PageDiffResult result) {
		List<ImageLob> baseImageList = baseImageThread.getImageLobList();
		List<ImageLob> testImageList = testImageThread.getImageLobList();
		
		for (int i = 0; i < baseImageList.size(); i++) {
			ImageLob baseImage = baseImageList.get(i);
			ImageLob testImage = this.findImageLob(baseImage, testImageList);
	
			DiffContent diffContent = new DiffContent(DiffContent.Category.Image);
			if (!this.diff(baseImage, testImage, diffContent)) {
				result.append(diffContent);
			}
			if (testImage != null) {
				testImageList.remove(testImage);
			}
		}
		
		// process remain images in test
		for (ImageLob image : testImageList) {
			DiffContent diffContent = new DiffContent(DiffContent.Category.Image);
			if (!this.diff(null, image, diffContent)) {
				result.append(diffContent);
			}
		}
	}
	
	private ImageLob findImageLob(ImageLob base, List<ImageLob> testImageList) {
		for (ImageLob test : testImageList) {
			if (base.getBBox().intersects(test.getBBox())) {
				return test;
			}
		}
		return null;
	}
	
	private boolean diff(ImageLob baseImage, ImageLob testImage, DiffContent entry) {
		Rectangle bbox_1 = baseImage == null ? null : baseImage.getBBox();
		Rectangle bbox_2 = testImage == null ? null : testImage.getBBox();
		entry.setBBox(bbox_1, bbox_2);
		boolean result = true;
		
		Integer val_1 = baseImage == null ? null : baseImage.width;
		Integer val_2 = testImage == null ? null : testImage.width;
		boolean equals = diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Width, equals, val_1, val_2);
		
		val_1 = baseImage == null ? null : baseImage.height;
		val_2 = testImage == null ? null : testImage.height;
		equals = diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Height, equals, val_1, val_2);
		
		val_1 = baseImage == null ? null : baseImage.byteCount;
		val_2 = testImage == null ? null : testImage.byteCount;
		equals = diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Byte_count, equals, val_1, val_2);
		
		val_1 = baseImage == null ? null : baseImage.bitsPerComponent;
		val_2 = testImage == null ? null : testImage.bitsPerComponent;
		equals = diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Bits_Per_Component, equals, val_1, val_2);

		String s_1 = baseImage == null ? null : baseImage.suffix;
		String s_2 = testImage == null ? null : testImage.suffix;
		equals = diff(s_1, s_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Suffix, equals, s_1, s_2);
		
		s_1 = baseImage == null ? null : baseImage.decode;
		s_2 = testImage == null ? null : testImage.decode;
		equals = diff(s_1, s_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Decode, equals, s_1, s_2);
		
		Rectangle baseRect = baseImage == null ? null : baseImage.getBBox();
		Rectangle testRect = testImage == null ? null : testImage.getBBox();
		if (baseRect != null) {
			equals = baseRect.equals(testRect);
		} else {
			equals = false;
		}
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Frame_size, equals, asString(baseRect), asString(testRect));
		
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
				if (diff.text.length() == 1 && diff.text.charAt(0) == 0x20) {
					itest += 1;
					continue;
				}
				int from = itest;
				TextLob[] lobs = testTextThread.getTextLob(from, diff.text.length());
				itest += diff.text.length();
				
				List<TextLob> lobList = mergeLobs(lobs);
				for (TextLob lob : lobList) {
					DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
					diffContent.putAttr(DiffContent.Key.Attr_Text, false, "#INSERT#", lob.getText());
					diffContent.setBBox(null, lob.getBoundingBox());
					result.append(diffContent);
				}
			} else if (diff.operation == Operation.DELETE) {
				if (diff.text.length() == 1 && diff.text.charAt(0) == 0x20) {
					ibase += 1;
					continue;
				}
				int from = ibase;
				TextLob[] lobs = baseTextThread.getTextLob(from, diff.text.length());
				ibase += diff.text.length();
				
				List<TextLob> lobList = mergeLobs(lobs);
				for (TextLob lob : lobList) {
					DiffContent diffContent = new DiffContent(DiffContent.Category.Text);
					diffContent.putAttr(DiffContent.Key.Attr_Text, false, lob.getText(), "#DELETE#");
					diffContent.setBBox(lob.getBoundingBox(), null);
					result.append(diffContent);					
				}
			} else {
				int baseBegin = 0;
				int testBegin = 0;
				int walk = 0;
				while (true) {
					if (walk >= diff.text.length()) {
						ibase += baseBegin;
						itest += testBegin;
						break;
					}
					int baseLen = baseTextThread.lenToContentEnd(baseBegin + ibase);
					int testLen = testTextThread.lenToContentEnd(testBegin + itest);
					
					int slot = baseLen <= testLen ? baseLen : testLen;
					if (slot > diff.text.length() - walk) {
						slot = diff.text.length() - walk;
					}
					
					String text = diff.text.substring(walk, walk + slot);
					TextLob baseLob = baseTextThread.getTextLob(baseBegin + ibase, slot)[0];
					TextLob testLob = testTextThread.getTextLob(testBegin + itest, slot)[0];
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
	
	private static List<TextLob> mergeLobs(TextLob[] lobs) {
		List<TextLob> lobList = new ArrayList<TextLob>();
		TextLob last = null;
		for (int i = 0; i < lobs.length; i++) {
			if (i == 0) {
				last = lobs[i];
				lobList.add(last);
				continue;
			}
			if (!last.mergeLob(lobs[i])) {
				last = lobs[i];
				lobList.add(lobs[i]);
			}
		}
		return lobList;
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
		
		val_1 = textContent_1 == null ? null : textContent_1.getNonStrokingColorValue();
		val_2 = textContent_2 == null ? null : textContent_2.getNonStrokingColorValue();
		equals = diff(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Color, equals, val_1, val_2);
		
		
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
		return Math.abs(f1 - f2) <= 0.00001;
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
	
	private static String asString(Rectangle rect) {
		if (rect == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("x=" + rect.x);
		buf.append(", ");
		buf.append("y=" + rect.y);
		buf.append(", ");
		buf.append("width=" + rect.width);
		buf.append(", ");
		buf.append("height=" + rect.height);
		return buf.toString();
	}
}
