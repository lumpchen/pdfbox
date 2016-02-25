package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.tools.diff.document.PageContent;
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
			
			System.out.println("    FontName: " + text.getGraphicsStateDesc().textState.fontName);
			System.out.println("    Outline: " + text.getOutlineArea().getBounds());
		} else if (content.getType() == PageContent.Type.Path) {
			PathContent text = (PathContent) content;
			System.out.println(text.getTypeString() + ": " + text.showString());
			
			System.out.println("    ColorSpace: " + text.getGraphicsStateDesc().strokingColor.colorSpace);
			System.out.println("    Outline: " + text.getOutlineArea().getBounds());
		}
	}
}

