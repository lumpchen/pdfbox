package org.apache.pdfbox.tools.diff;

import org.apache.pdfbox.tools.diff.document.compare.CompareSetting;

public class DiffSetting {

	public float resolution = 96;
	public String previewImageFormat = "png";
	public boolean noReportOnSameResult = true;
	public boolean printReport = false;
	
	public CompareSetting compSetting;
	
	public static final DiffSetting getDefaultSetting() {
		DiffSetting setting = new DiffSetting();
		
		setting.resolution = 96;
		setting.previewImageFormat = "png";
		setting.noReportOnSameResult = true;
		setting.printReport = false;
		
		setting.compSetting = new CompareSetting();
		
		return setting;
	}
}
