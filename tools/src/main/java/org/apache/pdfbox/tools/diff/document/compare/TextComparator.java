package org.apache.pdfbox.tools.diff.document.compare;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageThread;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageThread.TextLob;
import org.apache.pdfbox.tools.diff.document.PageThread.TextThread;
import org.apache.pdfbox.tools.diff.document.compare.name.fraser.neil.plaintext.diff_match_patch.Diff;
import org.apache.pdfbox.tools.diff.document.compare.name.fraser.neil.plaintext.diff_match_patch.Operation;

public class TextComparator extends ContentComparator {
	
	public TextComparator(CompareSetting setting) {
		super(setting);
	}
	
	public DiffContent[] compare(TextThread baseTextThread, TextThread testTextThread) {

		String baseText = baseTextThread.getText();
		String testText = testTextThread.getText();
		LinkedList<Diff> diffs = TextDiffUtil.diffText(baseText, testText);
		if (diffs == null || diffs.isEmpty()) {
			return new DiffContent[0];
		}

		List<DiffContent> result = new ArrayList<DiffContent>();
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
					result.add(diffContent);
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
					result.add(diffContent);					
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
					if (!this.compare(baseLob.getContent(), testLob.getContent(), diffContent)) {
						diffContent.setBBox(baseLob.getBoundingBox(), testLob.getBoundingBox());
						result.add(diffContent);
					}
					
					baseBegin += slot;
					testBegin += slot;
					walk += slot;
				}
			}
		}
		return result.toArray(new DiffContent[result.size()]);
	}
	
	private boolean compare(TextContent textContent_1, TextContent textContent_2, DiffContent entry) {
		boolean result = true;
		
		String val_1 = textContent_1 == null ? null : textContent_1.getFontName();
		String val_2 = textContent_2 == null ? null : textContent_2.getFontName();
		boolean equals = compare(removeFontNameSuffix(val_1), removeFontNameSuffix(val_2));
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Font, equals, val_1, val_2);
		
		Float size_1 = textContent_1 == null ? null : textContent_1.getFontSize();
		Float size_2 = textContent_2 == null ? null : textContent_2.getFontSize();
		equals = compare(size_1, size_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Font_size, equals, size_1 == null ? null : size_1.toString(), 
				size_2 == null ? null : size_2.toString());
		
		val_1 = textContent_1 == null ? null : textContent_1.getNonStrokingColorspace();
		val_2 = textContent_2 == null ? null : textContent_2.getNonStrokingColorspace();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Stroke_Colorspace, equals, val_1, val_2);
		
		val_1 = textContent_1 == null ? null : textContent_1.getNonStrokingColorValue();
		val_2 = textContent_2 == null ? null : textContent_2.getNonStrokingColorValue();
		equals = compare(val_1, val_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Stroke_Color, equals, val_1, val_2);
		
		if (this.setting.enableTextPositionCompare) {
			Integer x_1 = textContent_1 == null ? null : textContent_1.getX();
			Integer x_2 = textContent_2 == null ? null : textContent_2.getX();
			equals = compare(x_1, x_2); 
			result &= equals;
			entry.putAttr(DiffContent.Key.Attr_Pos_X, equals, x_1, x_2);
			
			Integer y_1 = textContent_1 == null ? null : textContent_1.getY();
			Integer y_2 = textContent_2 == null ? null : textContent_2.getY();
			equals = compare(y_1, y_2);
			result &= equals;
			entry.putAttr(DiffContent.Key.Attr_Pos_Y, equals, y_1, y_2);
		}
		
		return result;
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

	@Override
	public DiffContent[] compare(PageThread basePageThread, PageThread testPageThread) {
		DiffContent[] diffs = this.compare(basePageThread.getTextThread(), testPageThread.getTextThread());
		return diffs;
	}
}
