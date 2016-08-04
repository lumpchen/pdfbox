package org.apache.pdfbox.accessibility.check.matterhorn;

public class MetadataCheckPoint extends CheckPoint {

	String failure_Condition_1 = "Document does not contain XMP metadata stream";
	
	String failure_Condition_2 = "The metadata stream in the Catalog dictionary does not include the PDF/UA identifier";
	
	String failure_Condition_3 = "Metadata stream does not contain dc:title";
	
}
