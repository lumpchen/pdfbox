package org.apache.pdfbox.tools.diff.document.compare;

public class CompareSetting {

	public boolean enableComparePath = true;
	public boolean enableMergePath = true;
	public boolean enableCompareAnnots = true;
	public boolean enableTextPositionCompare = true;
	
	public float toleranceOfPath = 2;
	
	public CompareSetting() {
		this.enableCompareAnnots = true;
		this.enableComparePath = true;
		this.enableMergePath = true;
		this.enableTextPositionCompare = true;
		this.toleranceOfPath = 2;
	}
	
}
