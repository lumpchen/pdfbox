package org.apache.pdfbox.tools.diff;

public class DiffSetting {

	public float resolution = 96;
	public String previewImageFormat = "png";
	
	public static final DiffSetting getDefaultSetting() {
		DiffSetting setting = new DiffSetting();
		
		setting.resolution = 96;
		setting.previewImageFormat = "png";
		
		return setting;
	}
}
