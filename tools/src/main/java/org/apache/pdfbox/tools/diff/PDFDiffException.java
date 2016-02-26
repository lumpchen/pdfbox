package org.apache.pdfbox.tools.diff;

public class PDFDiffException extends Exception {
	
	private static final long serialVersionUID = -3060377057814832455L;

	public PDFDiffException(String msg) {
		super(msg);
	}
	
	public PDFDiffException(String msg, Throwable t) {
		super(msg, t);
	}
}
