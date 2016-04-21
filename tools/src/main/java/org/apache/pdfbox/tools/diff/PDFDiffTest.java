package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.report.HtmlDiffReport;

public class PDFDiffTest {

	public static void main(String[] args) {
		if (args.length == 2) {
			diff(args[0], args[1], "C:/uatest/report");
		} else {
			diff_folder(args[0], args[1], args[2]);			
		}
	}
	
	private static void diff(String base, String test, String reportDir) {
		diff(new File(base), new File(test), new File(reportDir));
	}
	
	private static void diff(File base, File test, File reportDir) {
		DiffSetting setting = DiffSetting.getDefaultSetting();
		PDFDiff differ = new PDFDiff(base, test, setting);
		try {
			System.out.println("Compare PDF: " + base.getName());
			PDocDiffResult result = differ.diff();
			
			int count = result.countOfDiffPages();
			if (count > 0) {
				System.out.println(count);
			} else {
				System.out.println("PDFs are same!");
				if (setting.noReportOnSameResult) {
					return;
				}
			}
			
			if (!reportDir.exists() && !reportDir.mkdirs()) {
				throw new IOException("Can't create report folder: " + reportDir.getAbsolutePath());
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

			HtmlDiffReport report = new HtmlDiffReport(reportDir, "report", result);
			report.toHtml();
		} catch (PDFDiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void diff_folder(String base, String test, String report) {
		File baseDir = new File(base);
		File testDir = new File(test);
		File reportDir = new File(report);
		
		File[] baseFiles = baseDir.listFiles();
		for (File baseFile : baseFiles) {
			String name = baseFile.getName();
			if (!name.toLowerCase().endsWith(".pdf")) {
				continue;
			}
			File testFile = new File(testDir, name);
			if (!testFile.exists()) {
				continue;
			}
			
			File reportFile = new File(reportDir, name);
			diff(baseFile, testFile, reportFile);				
		}
	}
}
