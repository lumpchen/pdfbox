package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.tools.diff.document.PageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.AnnotContent;
import org.apache.pdfbox.tools.diff.document.PageContent.ImageContent;
import org.apache.pdfbox.tools.diff.document.PageContent.PathContent;
import org.apache.pdfbox.tools.diff.document.PageContent.TextContent;
import org.apache.pdfbox.tools.diff.document.PageContentExtractor;

public class PageContentExtractorTest {

	public static void main(String[] args) {
		extract(new File(args[0]));
	}

	public static void extract(File pdf) {
		PDDocument pdfDoc = null;
		try {
			pdfDoc = PDDocument.load(pdf);
			
			int n = pdfDoc.getNumberOfPages();
			for (int i = 0; i < n; i++) {
				PDPage page = pdfDoc.getPage(i);
				List<PageContent> pageContents = extractPage(page);
				showPageContents(pageContents);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pdfDoc != null) {
				try {
					pdfDoc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static List<PageContent> extractPage(PDPage page) throws IOException {
		PageContentExtractor extractor = new PageContentExtractor(page);
		extractor.extract();
		return extractor.getPageContentList();
	}
	
	static void showPageContents(List<PageContent> pageContents) {
		if (pageContents == null || pageContents.isEmpty()) {
			return;
		}
		
		System.out.println(pageContents.size());
		for (PageContent content : pageContents) {
			showContent(content);
		}
	}
	
	static void showContent(PageContent content) {
		if (content.getType() == PageContent.Type.Text) {
			TextContent text = (TextContent) content;
			System.out.println(text.getTypeString() + ": " + text.showString());
			
			System.out.println("    FontName: " + text.getGraphicsStateDesc().textState.fontName
					+ " " + text.getGraphicsStateDesc().textState.fontSize);
			System.out.println("    Outline: " + text.getOutlineArea().getBounds());
		} else if (content.getType() == PageContent.Type.Path) {
			PathContent text = (PathContent) content;
			System.out.println(text.getTypeString() + ": " + text.showString());
			
			System.out.println("    ColorSpace: " + text.getGraphicsStateDesc().strokingColor.colorSpace);
			System.out.println("    Outline: " + text.getOutlineArea().getBounds());
		} else if (content.getType() == PageContent.Type.Image) {
			ImageContent image = (ImageContent) content;
			System.out.println(image.getTypeString() + ": " + image.showString());
			
			System.out.println("    ColorSpace: " + image.colorSpace);
			System.out.println("    Decode: " + image.decode);
			System.out.println("    Height: " + image.height);
			System.out.println("    Width: " + image.width);
			System.out.println("    RasterSize: " + image.byteCount);
			System.out.println("    Suffix: " + image.suffix);
		} else if (content.getType() == PageContent.Type.Annot) {
			AnnotContent annot = (AnnotContent) content;
			System.out.println(annot.getTypeString() + ": " + annot.showString());
			
			System.out.println("    SubType: " + annot.subType);
			System.out.println("    FieldType: " + annot.fieldType);
			System.out.println("    Name: " + annot.annotName);
			System.out.println("    Contents: " + annot.annotContents);
		} 
	}
}

