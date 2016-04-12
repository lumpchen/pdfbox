package org.apache.pdfbox.tools.diff.document.compare.geom;

import java.awt.geom.Point2D;

public class JTest {
	public static void main(String[] args) {
		Point2D v1 = new Point2D.Float(0, 1);
		Point2D v2 = new Point2D.Float(3, 1);

		java.awt.geom.Line2D.Float line1 = new java.awt.geom.Line2D.Float(v1, v2);

		Point2D v3 = new Point2D.Float(100, 5);
		Point2D v4 = new Point2D.Float(104, 5);
		java.awt.geom.Line2D.Float line2 = new java.awt.geom.Line2D.Float(v3, v4);
		
	}
}
