package org.apache.pdfbox.tools.diff;

import java.io.File;

public class PDFDiffTest {

	public static void main(String[] args) {
		diff(args[0], args[1]);
	}
	
	private static void diff(String base, String test) {
		PDFDiff differ = new PDFDiff(new File(base), new File(test), null);
		try {
			PDocDiffResult result = differ.diff();
			
			int count = result.countOfDiffPages();
			if (count > 0) {
				System.out.println(count);
			} else {
				System.out.println("PDFs are same!");
			}
		} catch (PDFDiffException e) {
			e.printStackTrace();
		}
	}
}
