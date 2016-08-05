package org.apache.pdfbox.accessibility.validate;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;

public class MetadataCheckProcess extends AbstractCheckProcess {

	@Override
	public void check(UACheckContext ctx) throws CheckProcessException {
		PDDocument doc = ctx.getDocument();

		PDDocumentCatalog catalog = doc.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();

		InputStream is = null;
		try {
			is = metadata.exportXMPMetadata();
			DomXmpParser xmpParser = new DomXmpParser();
			xmpParser.setStrictParsing(false);
			XMPMetadata xmp = xmpParser.parse(is);
			
			ctx.setMetadata(xmp);
			String part = this.checkPart(xmp);
			ctx.logger.fine(part);
			String title = this.checkTitle(xmp);
			ctx.logger.info(title);
		} catch (CheckProcessException e) {
			throw e;
		} catch (Exception e) {
			this.addCheckPoint(MatterhornProtocol.Metadata.CP_001);
			throw new CheckProcessException(MatterhornProtocol.Metadata.Failure_Condition_1, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new CheckProcessException("XMP metadata parsing error: ", e);
				}
			}
		}
	}
	
	private String checkPart(XMPMetadata xmp) throws CheckProcessException {
		try {
			String part = xmp.getPDFUAIdentificationSchema().getPartProperty().getStringValue();
			if (part == null) {
				this.addCheckPoint(MatterhornProtocol.Metadata.CP_002);				
			}
			return part;
		} catch (Exception e) {
			this.addCheckPoint(MatterhornProtocol.Metadata.CP_002);
			throw new CheckProcessException(MatterhornProtocol.Metadata.Failure_Condition_2, e);
		}
	}

	private String checkTitle(XMPMetadata xmp) throws CheckProcessException {
		try {
			String title = xmp.getDublinCoreSchema().getTitle();
			if (title == null) {
				this.addCheckPoint(MatterhornProtocol.Metadata.CP_003);				
			}
			return title;
		} catch (Exception e) {
			this.addCheckPoint(MatterhornProtocol.Metadata.CP_003);
			throw new CheckProcessException(MatterhornProtocol.Metadata.Failure_Condition_3, e);
		}
	}
}
