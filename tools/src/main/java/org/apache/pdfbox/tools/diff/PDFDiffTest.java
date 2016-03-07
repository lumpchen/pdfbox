package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.report.DiffReport;

public class PDFDiffTest {

	public static void main(String[] args) {
		diff(args[0], args[1]);
	}
	
	private static void diff(String base, String test) {
		PDFDiff differ = new PDFDiff(new File(base), new File(test), DiffSetting.getDefaultSetting());
		try {
			PDocDiffResult result = differ.diff();
			
			int count = result.countOfDiffPages();
			if (count > 0) {
				System.out.println(count);
			} else {
				System.out.println("PDFs are same!");
			}
			Integer[] nums = result.getDiffPageNums();
			for (int num : nums) {
				PageDiffResult pageDiffResult = result.getPageDiffResult(num);
				List<DiffContent> contentList = pageDiffResult.getContentList();
				
				if (contentList != null) {
					for (DiffContent content : contentList) {
						System.out.println(content);
					}
				}
			}
			DiffReport report = new DiffReport(new File("C:/uatest/report"), "report", result);
			report.toHtml();
		} catch (PDFDiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
