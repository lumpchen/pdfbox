package org.apache.pdfbox.tools.diff.document.compare;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult;
import org.apache.pdfbox.tools.diff.PageDiffResult.ContentAttr;
import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.document.PageThread.AnnotLob;
import org.apache.pdfbox.tools.diff.document.PageThread.AnnotSet;

public class AnnotComparator extends BaseObjectComparator {

	public AnnotComparator(CompareSetting setting) {
		super(setting);
	}
	
	public DiffContent[] compare(AnnotSet baseAnnotSet, AnnotSet testAnnotSet) {
		List<AnnotLob> baseAnnotList = baseAnnotSet.getAnnotLobList();
		List<AnnotLob> testAnnotList = testAnnotSet.getAnnotLobList();
		List<DiffContent> result = new ArrayList<DiffContent>();
		
		for (int i = 0; i < baseAnnotList.size(); i++) {
			AnnotLob baseAnnot = baseAnnotList.get(i);
			AnnotLob testAnnot = this.findAnnotLob(baseAnnot, testAnnotList);
			
			DiffContent diffContent = new DiffContent(DiffContent.Category.Annot);
			if (!this.diff(baseAnnot, testAnnot, diffContent)) {
				result.add(diffContent);
			}
			if (testAnnot != null) {
				testAnnotList.remove(testAnnot);
			}
		}
		
		// process remain annots in test
		for (AnnotLob annot : testAnnotList) {
			DiffContent diffContent = new DiffContent(DiffContent.Category.Annot);
			if (!this.diff(null, annot, diffContent)) {
				result.add(diffContent);
			}
		}
		
		return result.toArray(new DiffContent[result.size()]);
	}
	
	private boolean diff(AnnotLob baseAnnot, AnnotLob testAnnot, DiffContent entry) {
		Rectangle bbox_1 = baseAnnot == null ? null : baseAnnot.getBBox();
		Rectangle bbox_2 = testAnnot == null ? null : testAnnot.getBBox();
		entry.setBBox(bbox_1, bbox_2);
		boolean result = true;
		
		String s_1 = baseAnnot == null ? null : baseAnnot.fieldType;
		String s_2 = testAnnot == null ? null : testAnnot.fieldType;
		boolean equals = diff(s_1, s_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_FieldType, equals, s_1, s_2);
		
		s_1 = baseAnnot == null ? null : baseAnnot.subType;
		s_2 = testAnnot == null ? null : testAnnot.subType;
		equals = diff(s_1, s_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_SubType, equals, s_1, s_2);
		
		s_1 = baseAnnot == null ? null : baseAnnot.annotName;
		s_2 = testAnnot == null ? null : testAnnot.annotName;
		equals = diff(s_1, s_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_AnnotName, equals, s_1, s_2);
		
		s_1 = baseAnnot == null ? null : baseAnnot.annotContents;
		s_2 = testAnnot == null ? null : testAnnot.annotContents;
		equals = diff(s_1, s_2);
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_AnnotContents, equals, s_1, s_2);
		
		Rectangle baseRect = baseAnnot == null ? null : baseAnnot.getBBox();
		Rectangle testRect = testAnnot == null ? null : testAnnot.getBBox();
		if (baseRect != null) {
			equals = baseRect.equals(testRect);
		} else {
			equals = false;
		}
		result &= equals;
		entry.putAttr(DiffContent.Key.Attr_Annot_Rect, equals, asString(baseRect), asString(testRect));
		
		PageContentComparator PageContentComparator = new PageContentComparator(this.setting);
		PageDiffResult appearanceDiffResult = PageContentComparator.compare(baseAnnot.getAppearance(), testAnnot.getAppearance());
		if (appearanceDiffResult.count() > 0) {
			result &= false;
			entry.putAttr(DiffContent.Key.Attr_Annot_Appearance, false, "", "");
			
			List<DiffContent> contentList = appearanceDiffResult.getContentList();
			for (DiffContent content : contentList) {
				if (content.getCategory() == DiffContent.Category.Text) {
					List<ContentAttr> attrList = content.getAttrList();
					if (attrList.size() > 0) {
						entry.putAttr("|-----Text", false, "", "");
					}
					for (ContentAttr attr : attrList) {
						entry.putAttr("|----------" + attr.key, attr.equals, attr.baseVal, attr.testVal);
					}
				}
				
				if (content.getCategory() == DiffContent.Category.Image) {
				}
				
				if (content.getCategory() == DiffContent.Category.Path) {
				}
				
				if (content.getCategory() == DiffContent.Category.Annot) {
				}
			}			
		}
		
		return result;
	}
	private AnnotLob findAnnotLob(AnnotLob base, List<AnnotLob> testAnnotList) {
		for (AnnotLob test : testAnnotList) {
			if (diff(base.fieldType, test.fieldType) 
					&& diff(base.subType, test.subType)
					&& base.getBBox().intersects(test.getBBox())
					) {
				return test;
			}
		}
		return null;
	}

	@Override
	public DiffContent[] compare(Object base, Object test) {
		return this.compare((AnnotSet) base, (AnnotSet) test);
	}
}
