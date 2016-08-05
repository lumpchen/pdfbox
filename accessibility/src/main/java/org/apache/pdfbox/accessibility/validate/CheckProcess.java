package org.apache.pdfbox.accessibility.validate;

public interface CheckProcess {

	public void check(UACheckContext ctx) throws CheckProcessException;
	
}
