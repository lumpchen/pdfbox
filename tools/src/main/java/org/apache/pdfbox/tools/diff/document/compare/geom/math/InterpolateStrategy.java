
package org.apache.pdfbox.tools.diff.document.compare.geom.math;

/**
 * Defines a generic function to interpolate 2 float values.
 */
public interface InterpolateStrategy {

	/**
	 * Implements an interpolation equation using double precision values.
	 * 
	 * @param a
	 *            current value
	 * @param b
	 *            target value
	 * @param f
	 *            normalized interpolation factor (0.0 .. 1.0)
	 * @return interpolated value
	 */
	public double interpolate(double a, double b, double f);

	/**
	 * Implements an interpolation equation using float values.
	 * 
	 * @param a
	 *            current value
	 * @param b
	 *            target value
	 * @param f
	 *            normalized interpolation factor (0.0 .. 1.0)
	 * @return interpolated value
	 */
	public float interpolate(float a, float b, float f);
}
