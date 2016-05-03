package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.report.HtmlDiffReport;

public class PDFDiffTool {

	public static void main(String[] args) {
		if (args.length == 3) {
			diff(args[0], args[1], args[2]);
		} else {
			showUsage();
		}
	}
	
	private static void showUsage() {
		
	}
	
	public static int diff(String base, String test, String reportDir) {
		File baseFile = new File(base);
		if (baseFile.exists() && baseFile.isFile()) {
			return diff(new File(base), new File(test), new File(reportDir));
		} else {
			return diff_folder(base, test, reportDir);
		}
	}
	
	public static int diff(File base, File test, File reportDir) {
		return diff(base, test, reportDir, null, null);
	}
	
	public static int diff(File base, File test, File reportDir, DiffSetting setting, DiffLogger logger) {
		if (setting == null) {
			setting = DiffSetting.getDefaultSetting();	
		}
		
		if (logger == null) {
			logger = DiffLogger.getDefaultLogger();
		}
		
		PDFDiff differ = new PDFDiff(base, test, setting, logger);
		try {
			logger.info("Compare PDF: " + base.getName() + " To " + test.getName());
			PDocDiffResult result = differ.diff();
			
			int count = result.countOfDiffPages();
			if (count > 0) {
				logger.info("Found " + count + " different " + (count == 1 ? "page" : "pages"));
			} else {
				logger.info("PDFs are same!");
				if (setting.noReportOnSameResult) {
					return 0;
				}
			}
			
			if (!reportDir.exists() && !reportDir.mkdirs()) {
				logger.error("Fail to create report folder.");
				throw new IOException("Can't create report folder: " + reportDir.getAbsolutePath());
			}
			
			Integer[] nums = result.getDiffPageNums();
			for (int num : nums) {
				PageDiffResult pageDiffResult = result.getPageDiffResult(num);
				List<DiffContent> contentList = pageDiffResult.getContentList();
				
				if (contentList != null) {
					for (DiffContent content : contentList) {
						logger.info(content.toString());
					}
				}
			}

			HtmlDiffReport report = new HtmlDiffReport(reportDir, "report", result);
			report.toHtml();
			return count;
		} catch (PDFDiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static int diff_folder(String base, String test, String report) {
		File baseDir = new File(base);
		File testDir = new File(test);
		File reportDir = new File(report);
		
		int count = 0;
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
			count += diff(baseFile, testFile, reportFile);				
		}
		
		return count;
	}
}
