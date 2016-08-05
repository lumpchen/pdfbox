package org.apache.pdfbox.accessibility.validate;

public class MatterhornProtocol {

	public static final class Metadata {
		public static final String category = "Metadata";
		public static final String Failure_Condition_1 = "Document does not contain XMP metadata stream";
		public static final String Failure_Condition_2 = "The metadata stream in the Catalog dictionary does not include the PDF/UA identifier.";
		public static final String Failure_Condition_3 = "Metadata stream does not contain dc:title";
		
		public static CheckPoint CP_001 = new CheckPoint(category, Failure_Condition_1);
		public static CheckPoint CP_002 = new CheckPoint(category, Failure_Condition_2);
		public static CheckPoint CP_003 = new CheckPoint(category, Failure_Condition_3);
	}
}
