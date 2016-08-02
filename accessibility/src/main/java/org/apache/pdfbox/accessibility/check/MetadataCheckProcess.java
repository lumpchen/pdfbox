package org.apache.pdfbox.accessibility.check;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.accessibility.UACheckContext;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
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
			is = getXpacket(doc.getDocument());
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

	private static InputStream getXpacket(COSDocument cdocument) throws IOException {
		COSObject catalog = cdocument.getCatalog();
		COSBase cb = catalog.getDictionaryObject(COSName.METADATA);
		if (cb == null) {
			// missing Metadata Key in catalog
		}
		// no filter key
		COSDictionary metadataDictionnary = getAsDictionary(cb, cdocument);
		if (metadataDictionnary.getItem(COSName.FILTER) != null) {
			// should not be defined
		}

		if (!(metadataDictionnary instanceof COSStream)) {
		}

		COSStream stream = (COSStream) metadataDictionnary;
		return stream.createInputStream();
	}

	public static COSDictionary getAsDictionary(COSBase cbase, COSDocument cDoc) {
		if (cbase instanceof COSObject) {
			try {
				COSObjectKey key = new COSObjectKey((COSObject) cbase);
				COSObject obj = cDoc.getObjectFromPool(key);
				if (obj != null && obj.getObject() instanceof COSDictionary) {
					return (COSDictionary) obj.getObject();
				} else {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
		} else if (cbase instanceof COSDictionary) {
			return (COSDictionary) cbase;
		} else {
			return null;
		}
	}
}
