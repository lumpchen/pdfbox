package org.apache.pdfbox.accessibility.check.matterhorn;

public abstract class CheckPoint {

	protected String category;
	protected String failureCondition;

	public CheckPoint() {
	}

	public String getExplanation() {
		return "";
	}
	
	public String getFailureCondition() {
		return this.failureCondition;
	}
}
