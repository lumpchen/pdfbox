
package org.apache.pdfbox.tools.diff.document.compare.geom.math;

/**
 * Implementation of the linear interpolation function
 * 
 * i = a + ( b - a ) * f
 */
public class LinearInterpolation implements InterpolateStrategy {

	public double interpolate(double a, double b, double f) {
		return a + (b - a) * f;
	}

	public final float interpolate(float a, float b, float f) {
		return a + (b - a) * f;
	}
}
