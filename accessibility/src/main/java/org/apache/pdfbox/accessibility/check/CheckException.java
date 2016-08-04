package org.apache.pdfbox.accessibility.check;

public class CheckException extends Exception {

	private static final long serialVersionUID = -8560861260489364632L;

	public CheckException(String msg) {
		super(msg);
	}
	
	public CheckException(Exception e) {
		super(e);
	}
	
	public CheckException(String msg, Exception e) {
		super(msg, e);
	}
}
