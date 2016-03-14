package org.apache.pdfbox.tools.diff.document;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

public class PageCrop {

	private PDDocument pdfDoc;
	
	public PageCrop(File src) throws IOException {
		this.pdfDoc = PDDocument.load(src);
	}

	public void crop(File dest) throws IOException {
		int pageCount = pdfDoc.getNumberOfPages();
		try {
			for (int i = 0; i < pageCount; i++) {
				System.out.print("Processing page: " + i + "... ");
				PDPage page = pdfDoc.getPage(i);
				cropPage(page);
				System.out.println("Done");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.pdfDoc.save(dest);
		System.out.println("Finished!");
	}

	private void cropPage(PDPage page) throws IOException {
		PageContentExtractor extractor = new PageContentExtractor(page);
		extractor.extract();
		List<PageContent> pageContents = extractor.getPageContentList();
		Area cropArea = new Area();
		for (PageContent content : pageContents) {
			Area area = content.getOutlineArea();
			if (area != null) {
				cropArea.add(area);
			}
		}
		PDFStreamParser parser = new PDFStreamParser(page);
		parser.parse();
		List<Object> tokens = parser.getTokens();
		List<Object> newTokens = new ArrayList<Object>();
		PDRectangle bbox = page.getBBox();
		Rectangle rect = cropArea.getBounds();
		bbox = new PDRectangle(rect.width + 4, rect.height + 4);
		page.setCropBox(bbox);

		newTokens.add(Operator.getOperator("q"));
		newTokens.add(COSInteger.get(1));
		newTokens.add(COSInteger.get(0));
		newTokens.add(COSInteger.get(0));
		newTokens.add(COSInteger.get(1));
		newTokens.add(COSInteger.get(-rect.x + 2));
		newTokens.add(COSInteger.get(-rect.y + 2));
		newTokens.add(Operator.getOperator("cm"));
		
		newTokens.addAll(tokens);
		
		newTokens.add(Operator.getOperator("Q"));
		
		PDStream newContents = new PDStream(pdfDoc);
		OutputStream out = newContents.createOutputStream(COSName.FLATE_DECODE);
		ContentStreamWriter writer = new ContentStreamWriter(out);
		writer.writeTokens(newTokens);
		out.close();
		page.setContents(newContents);
	}
	
	public static void main(String[] args) throws IOException {
		PageCrop cropper = new PageCrop(new File(args[0]));
		cropper.crop(new File(args[1]));
	}
}
