package org.apache.pdfbox.tools.diff.document.compare;

public class CompareSetting {

	public boolean enableComparePath = true;
	public boolean enableCompareAnnots = true;
	
	public CompareSetting() {
		this.enableCompareAnnots = true;
		this.enableComparePath = false;
	}
	
}
