package org.apache.pdfbox.accessibility.validate;

public class CheckProcessException extends Exception {

	private static final long serialVersionUID = 6700623044280646095L;

	public enum Type {
		Program, checkState
	};
	
	public CheckProcessException(String message, Throwable t) {
		super(message, t);
	}
	
}
