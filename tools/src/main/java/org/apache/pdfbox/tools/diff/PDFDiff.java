package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageContentExtractor;

public class PDFDiff {

	private File base;
	private File test;
	private DiffSetting setting;
	
	public PDFDiff(File base, File test, DiffSetting setting) {
		this.base = base;
		this.test = test;
		this.setting = setting;
	}
	
	public PDocDiffResult diff() throws PDFDiffException {
		PDDocument baselinePDF = null;
		PDDocument testPDF = null;
		
		PDocDiffResult result = new PDocDiffResult();
		try {
			baselinePDF = PDDocument.load(this.base);
			testPDF = PDDocument.load(this.test);
			this.diffPDoc(baselinePDF, testPDF, result);
		} catch (Exception e) {
			throw new PDFDiffException("Diff error: ", e);
		} finally {
			if (baselinePDF != null) {
				try {
					baselinePDF.close();
				} catch (IOException e) {
					throw new PDFDiffException("Diff error: ", e);
				}
			}
			if (testPDF != null) {
				try {
					testPDF.close();
				} catch (IOException e) {
					throw new PDFDiffException("Diff error: ", e);
				}
			}
		}
		return result;
	}
	
	private void diffPDoc(PDDocument base, PDDocument test, PDocDiffResult result) throws PDFDiffException {
		int pageNum_1 = base.getNumberOfPages();
		int pageNum_2 = test.getNumberOfPages();
		if (pageNum_1 != pageNum_2) {
			throw new PDFDiffException("Page count is different: base=" + pageNum_1 + ", test=" + pageNum_2);
		}
		
        for (int i = 0; i < pageNum_1; i++) {
            PDPage page_1 = base.getPage(i);
            PDPage page_2 = test.getPage(i);
            this.diffPage(i + 1, page_1, page_2, result);
        }
	}
	
	private void diffPage(int pageNo, PDPage base, PDPage test, PDocDiffResult result) throws PDFDiffException {
		try {
			PageContentExtractor extractor_1 = new PageContentExtractor(base);
			extractor_1.extract();
			List<PageContent> basePageContents = extractor_1.getPageContentList();
			PageContentSet basePageContentSet = new PageContentSet(pageNo, basePageContents);
			
			PageContentExtractor extractor_2 = new PageContentExtractor(test);
			extractor_2.extract();
			List<PageContent> testPageContents = extractor_2.getPageContentList();
			PageContentSet testPageContentSet = new PageContentSet(pageNo, testPageContents);
			
			PageContentDiff differ = new PageContentDiff(this.setting);
			PageDiffResult pageDiffResult = differ.diff(basePageContentSet, testPageContentSet);
			
			result.add(pageNo, pageDiffResult);
		} catch (IOException e) {
			throw new PDFDiffException("Page content extract failure: " + pageNo);
		}
	}
}
