package org.apache.pdfbox.tools.diff;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.diff.PDocDiffResult.PageInfo;
import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageContentExtractor;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

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
			result.getBaseDocumentInfo().setCategory("base");
			result.getBaseDocumentInfo().setTitle(this.base.getName());
					
			testPDF = PDDocument.load(this.test);
			result.getTestDocumentInfo().setCategory("test");
			result.getTestDocumentInfo().setTitle(this.test.getName());
			
			this.diffPDoc(baselinePDF, testPDF, result);
		} catch (Exception e) {
			e.printStackTrace();
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
		result.getBaseDocumentInfo().setPageCount(pageNum_1);
		result.getBaseDocumentInfo().setImageSuffix(setting.previewImageFormat);
		
		result.getTestDocumentInfo().setPageCount(pageNum_2);
		result.getTestDocumentInfo().setImageSuffix(setting.previewImageFormat);
        try {
            for (int i = 0; i < pageNum_1; i++) {
                PDPage page_1 = base.getPage(i);
                PDPage page_2 = test.getPage(i);
                this.diffPage(i, page_1, page_2, result);
                
                PageInfo info = new PageInfo(i);
                info.setPreviewImage(this.renderPage(i, base));
                int[] size = this.getPageSize(page_1);
                info.setWidth(size[0]);
                info.setHeight(size[1]);
        		result.getBaseDocumentInfo().setPageInfo(i, info);
        		
        		info = new PageInfo(i);
        		info.setPreviewImage(this.renderPage(i, test));
        		size = this.getPageSize(page_2);
                info.setWidth(size[0]);
                info.setHeight(size[1]);
        		result.getTestDocumentInfo().setPageInfo(i, info);
            }
        } catch (Exception e) {
        	throw new PDFDiffException("Can't render page: " + e);
        }
	}
	
	private int[] getPageSize(PDPage page) {
        PDRectangle cropbBox = page.getCropBox();
        float widthPt = cropbBox.getWidth();
        float heightPt = cropbBox.getHeight();
        float scale = this.setting.resolution / 72f;
        int widthPx = Math.round(widthPt * scale);
        int heightPx = Math.round(heightPt * scale);
        int rotationAngle = page.getRotation();

        if (rotationAngle == 90 || rotationAngle == 270) {
            return new int[]{heightPx, widthPx};
        } else{
        	return new int[]{widthPx, heightPx};
        }
	}
	
	private String renderPage(int pageNo, PDDocument pdoc) throws Exception {
		PDFRenderer baseRenderer = new PDFRenderer(pdoc);
        BufferedImage image = baseRenderer.renderImageWithDPI(pageNo, this.setting.resolution, ImageType.RGB);
        File temp = File.createTempFile("pdf_diff", "." + setting.previewImageFormat);
        if (ImageIOUtil.writeImage(image, temp.getAbsolutePath(), (int) this.setting.resolution)) {
        	return temp.getAbsolutePath();
        }
        throw new PDFDiffException("Can't render page: " + pageNo);
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
