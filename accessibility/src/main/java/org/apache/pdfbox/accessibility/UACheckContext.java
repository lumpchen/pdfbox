package org.apache.pdfbox.accessibility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.XMPMetadata;

public class UACheckContext {

	private PDDocument document;
	private XMPMetadata metadata;
	private CheckResult checkResult;
	private UACheckConfiguration conf;
	
	public UACheckContext() {
	}

	public void setConfigure(UACheckConfiguration conf) {
		this.conf = conf;
	}
	
	public UACheckConfiguration getConfigure() {
		return this.conf;
	}
	
	public void setDocument(PDDocument document) {
		this.document = document;
	}

	public PDDocument getDocument() {
		return this.document;
	}

	public void setCheckResult(CheckResult checkResult) {
		this.checkResult = checkResult;
	}

	public CheckResult getCheckResult() {
		return this.checkResult;
	}

	public XMPMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(XMPMetadata metadata) {
		this.metadata = metadata;
	}

}
