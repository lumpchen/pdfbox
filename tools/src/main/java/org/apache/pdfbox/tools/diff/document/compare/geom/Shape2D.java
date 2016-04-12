
package org.apache.pdfbox.tools.diff.document.compare.geom;

import java.util.List;

/**
 * Interface description of common operations supported by 2D geometry types.
 */
public interface Shape2D {

	/**
	 * Checks if the point is within the given shape.
	 * 
	 * @return true, if inside
	 */
	boolean containsPoint(ReadonlyVec2D p);

	/**
	 * Computes the area of the shape.
	 * 
	 * @return area
	 */
	float getArea();

	/**
	 * Returns the shape's axis-aligned bounding rect.
	 * 
	 * @return bounding rect
	 */
	Rect getBounds();

	/**
	 * Computes the shape's circumference.
	 * 
	 * @return circumference
	 */
	float getCircumference();

	/**
	 * Returns a list of the shape's perimeter edges.
	 * 
	 * @return list of {@link Line2D} elements
	 */
	List<Line2D> getEdges();

	/**
	 * Computes a random point within the shape's perimeter.
	 * 
	 * @return Vec2D
	 */
	Vec2D getRandomPoint();
}
