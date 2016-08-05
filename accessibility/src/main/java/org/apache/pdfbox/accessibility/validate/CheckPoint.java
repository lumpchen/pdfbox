package org.apache.pdfbox.accessibility.validate;

public class CheckPoint {

	protected String category;
	protected String failureCondition;

	public CheckPoint(String category, String failureCondition) {
		this.category = category;
		this.failureCondition = failureCondition;
	}

	public String getCategory() {
		return this.category;
	}

	public String getFailureCondition() {
		return this.failureCondition;
	}
}
