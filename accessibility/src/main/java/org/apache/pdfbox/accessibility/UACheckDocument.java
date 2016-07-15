package org.apache.pdfbox.accessibility;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;

public class UACheckDocument extends PDDocument {

	private CheckResult result;

	private UACheckConfiguration config;

	public enum UAVersion {
		PDFUA1
	};

	public UACheckDocument(COSDocument doc) {
		this(doc, UAVersion.PDFUA1, UACheckConfiguration.createPdfUA1Configuration());
	}
	
	public UACheckDocument(COSDocument doc, UAVersion version, UACheckConfiguration config) {
		super(doc);
		this.config = config;
		
		this.result = new CheckResult();
	}
	
	
}
