package org.apache.pdfbox.tools.diff.document.compare;

public class CompareSetting {

	public boolean enableComparePath = true;
	public boolean enableMergePath = false;
	public boolean enableCompareAnnots = true;
	public boolean enableTextPositionCompare = true;
	
	public float toleranceOfPath = 2;
	
	public CompareSetting() {
		this.enableCompareAnnots = true;
		this.enableComparePath = true;
		this.enableMergePath = false;
		this.enableTextPositionCompare = true;
		this.toleranceOfPath = 2;
	}
	
}
