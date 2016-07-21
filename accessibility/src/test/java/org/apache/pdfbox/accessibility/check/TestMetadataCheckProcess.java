package org.apache.pdfbox.accessibility.check;

import org.apache.pdfbox.accessibility.UAChecker;
import org.junit.Test;

public class TestMetadataCheckProcess {
	
	@Test
	public void test() {
		UAChecker checker = new UAChecker();
		String path = "C:\\uatest\\PDFUA-Reference-Files_01\\PDFUA-Reference-10_(Danish-form).pdf";
		checker.check(path);
	}
}
