package org.apache.pdfbox.tools.diff.document.compare;

public class CompareSetting {

	public boolean enableComparePath = true;
	public boolean enableMergePath = false;
	public boolean enableCompareAnnots = true;
	public boolean enableTextPositionCompare = true;
	
	public float toleranceOfPath = 0.8f;
	public float toleranceOfPosition = 1;
	
	public CompareSetting() {
	}
	
}
