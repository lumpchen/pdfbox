package org.apache.pdfbox.tools.diff.document.compare.geom;

public class Test {

    public static void main(String[] args) {
        Vec2D v1 = new Vec2D(0, 1);
        Vec2D v2 = new Vec2D(3, 1);
        Line2D line1 = new Line2D(v1, v2);

        Vec2D v3 = new Vec2D(100, 5);
        Vec2D v4 = new Vec2D(104, 5);
        Line2D line2 = new Line2D(v3, v4);

        System.out.println(line1.distanceToPoint(v4));
        System.out.println(line1.distanceToPointSquared(v4));
        System.out.println(line1.intersectLine(line2).getType());
        System.out.println(line1.equals(line2));
        
        System.out.println(line1.closestPointTo(v4));
        System.out.println(line1.getDirection());
        
    }
}
