package org.apache.pdfbox.tools.diff;

import java.io.File;

public class PDFDiff {

	private File base;
	private File test;
	private DiffSetting setting;
	
	public PDFDiff(File base, File test, DiffSetting setting) {
		this.base = base;
		this.test = test;
		this.setting = setting;
	}
	
	public DiffResult diff() {
		return null;
	}
}
