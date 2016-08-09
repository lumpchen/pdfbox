package org.apache.pdfbox.accessibility.validate;

import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;

public class DocumentCatalogProcess extends AbstractCheckProcess {

	@Override
	public void check(UACheckContext ctx) throws CheckProcessException {
		ctx.logger.info(UACheckConfiguration.DOCUMENT_CATALOG_PROCESS);
		
		PDDocumentCatalog catalog = ctx.getDocument().getDocumentCatalog();
		if (catalog == null) {
			return;
		}
		
		PDStructureTreeRoot structureTreeRoot = catalog.getStructureTreeRoot();
		if (structureTreeRoot == null) {
			
		}
		
		PDViewerPreferences viewerPreferences = catalog.getViewerPreferences();
		if (viewerPreferences == null) {
			
		}
	}

}
