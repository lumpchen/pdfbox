package org.apache.pdfbox.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.tools.diff.PageDiffResult.DiffContent;
import org.apache.pdfbox.tools.diff.report.HtmlDiffReport;

public class PDFDiffTool {

	public static void main(String[] args) {
		if (args == null || args.length < 3) {
			showUsage();
		}
		
		boolean folderCompare = false;
		String base = null;
		String test = null;
		String result = null;
		DiffSetting setting = DiffSetting.getDefaultSetting();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			
			if (arg.equals("-folder")) {
				folderCompare = true;
			} else if (arg.equals("-disableTextPositionCompare")) {
				setting.compSetting.enableTextPositionCompare = false;
			} else if (arg.equals("-disableCompareAnnots")) {
				setting.compSetting.enableCompareAnnots = false;
			} else if (arg.equals("-disableComparePath")) {
				setting.compSetting.enableComparePath = false;
			} else if (arg.equals("-imageQuality")) {
				if (args[++i].equalsIgnoreCase("high")) {
					setting.resolution = 300;
				}
			} else if (base == null) {
				base = args[i];
			} else if (test == null) {
				test = args[i];
			} else if (result == null) {
				result = args[i];
			}
		}
		
		if (base == null || test == null || result == null) {
			System.err.println("Invalid parameters! \n");
			showUsage();
		}
		
		if (folderCompare) {
			diff_folder(base, test, result, setting);
		} else {
			diff(base, test, result, setting);
		}
	}
	
	private static void showUsage() {
        String message = "Usage: java -jar pdfdiff.jar [options] <baseline> <compare> <result_folder>\n"
                + "\nOptions:\n"
                + "  -folder                                  : Compare all pdf file in folder\n"
                + "  -disableCompareAnnots                    : Disable annotation compare\n"
                + "  -disableComparePath                      : Disable path compare\n"
                + "  -disableTextPositionCompare              : Disable text position compare\n"
                + "  -imageQuality                            : <high> as reolustion 300dpi\n";
        
        System.err.println(message);
        System.exit(1);
	}
	
	public static int diff(String base, String test, String reportDir, DiffSetting setting) {
		File baseFile = new File(base);
		if (baseFile.exists() && baseFile.isFile()) {
			return diff(new File(base), new File(test), new File(reportDir), setting);
		} else {
			return diff_folder(base, test, reportDir, setting);
		}
	}
	
	public static int diff(File base, File test, File reportDir, DiffSetting setting) {
		return diff(base, test, reportDir, setting, null);
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
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return -1;
	}
	
	public static int diff_folder(String base, String test, String report, DiffSetting setting) {
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
			count += diff(baseFile, testFile, reportFile, setting);
		}
		
		return count;
	}
}
