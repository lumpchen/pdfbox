package org.apache.pdfbox.accessibility.validate;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.XMPMetadata;

public class UACheckContext {

	private PDDocument document;
	private XMPMetadata metadata;
	private CheckResult checkResult;
	private UACheckConfiguration conf;
	public final Logger logger = Logger.getLogger(UACheckContext.class.getName());
	
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
	
	public void setLogFile(String logFilePath) throws SecurityException, IOException {
		FileHandler logFile = new FileHandler(logFilePath);
		logger.addHandler(logFile);
	}

}
