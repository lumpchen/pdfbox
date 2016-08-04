package org.apache.pdfbox.accessibility;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.pdfbox.accessibility.check.CheckException;
import org.apache.pdfbox.accessibility.check.CheckProcess;
import org.apache.pdfbox.accessibility.check.CheckProcessException;
import org.apache.pdfbox.pdmodel.PDDocument;

public class UAChecker {

	public UAChecker() {
	}

	public void check(String path) {
		UACheckContext ctx = new UACheckContext();

		PDDocument pdf = null;
		try {
			pdf = PDDocument.load(new File(path));

			ctx.setDocument(pdf);
			ctx.setConfigure(UACheckConfiguration.createPdfUA1Configuration());
			
			this.checkProcess(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CheckException e) {
			e.printStackTrace();
		} finally {
			if (pdf != null) {
				try {
					pdf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void checkProcess(UACheckContext ctx) throws CheckException {
		Collection<String> processes = ctx.getConfigure().getProcessNames();
		for (String name : processes) {
			CheckProcess p = ctx.getConfigure().getProcessInstance(name);
			
			try {
				p.check(ctx);
			} catch (CheckProcessException e) {
				e.printStackTrace();
			}
		}
	}

}
