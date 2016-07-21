package org.apache.pdfbox.accessibility.check;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.accessibility.UACheckContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;

public class MetadataCheckProcess extends AbstractCheckProcess {

	@Override
	public void check(UACheckContext ctx) throws CheckProcessException {
		PDDocument doc = ctx.getDocument();

		PDDocumentCatalog catalog = doc.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
	
		InputStream is = null;
		try {
			is = metadata.exportXMPMetadata();
			System.out.println(new String(metadata.toByteArray()));
	        DomXmpParser xmpParser = new DomXmpParser();
	        xmpParser.setStrictParsing(false);
	        XMPMetadata xmp = xmpParser.parse(is);
	        ctx.setMetadata(xmp);
	        System.out.println(xmp.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmpParsingException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		

	}

}
