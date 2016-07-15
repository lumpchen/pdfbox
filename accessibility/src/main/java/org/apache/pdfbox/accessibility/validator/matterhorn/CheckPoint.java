package org.apache.pdfbox.accessibility.validator.matterhorn;

public abstract class CheckPoint {

	protected String failureCondition;

	public CheckPoint() {
	}

	abstract public boolean check();
	
	public String getExplanation() {
		return "";
	}
	
	public String getFailureCondition() {
		return this.failureCondition;
	}
	
}
