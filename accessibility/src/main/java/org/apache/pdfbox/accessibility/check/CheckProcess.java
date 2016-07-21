package org.apache.pdfbox.accessibility.check;

import org.apache.pdfbox.accessibility.UACheckContext;

public interface CheckProcess {

	public void check(UACheckContext ctx) throws CheckProcessException;
	
}
