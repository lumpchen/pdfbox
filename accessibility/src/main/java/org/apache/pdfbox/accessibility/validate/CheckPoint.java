package org.apache.pdfbox.accessibility.validate;

public class CheckPoint {

	public enum CheckStatus {
		Passed, Warned, Failed
	};
	private String category;
	private String title;
	private CheckStatus status;

	public CheckPoint(String category, String title) {
		this.category = category;
		this.title = title;
	}

	public String getCategory() {
		return this.category;
	}

	public String getTitle() {
		return this.title;
	}
	
	public void setStatus(CheckStatus status) {
		this.status = status;
	}
	
	public CheckStatus getStatus() {
		return this.status;
	}
}
