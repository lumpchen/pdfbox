
package org.apache.pdfbox.tools.diff.document.compare.geom.math;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DoubleRange {

	public static DoubleRange fromSamples(double... samples) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (double s : samples) {
			min = MathUtils.min(min, s);
			max = MathUtils.max(max, s);
		}
		return new DoubleRange(min, max);
	}

	public static DoubleRange fromSamples(List<Double> samples) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (double s : samples) {
			min = MathUtils.min(min, s);
			max = MathUtils.max(max, s);
		}
		return new DoubleRange(min, max);
	}

	public double min, max;

	public double currValue;

	protected Random random = new Random();

	public DoubleRange() {
		this(0d, 1d);
	}

	public DoubleRange(double min, double max) {
		// swap if necessary...
		if (min > max) {
			double t = max;
			max = min;
			min = t;
		}
		this.min = min;
		this.max = max;
		this.currValue = min;
	}

	public double adjustCurrentBy(double val) {
		return setCurrent(currValue + val);
	}

	public DoubleRange copy() {
		DoubleRange range = new DoubleRange(min, max);
		range.currValue = currValue;
		range.random = random;
		return range;
	}

	/**
	 * Returns the value at the normalized position <code>(0.0 = min ... 1.0 =
	 * max-EPS)</code> within the range. Since the max value is exclusive, the
	 * value returned for position 1.0 is the range max value minus
	 * {@link MathUtils#EPS}. Also note the given position is not being clipped
	 * to the 0.0-1.0 interval, so when passing in values outside that interval
	 * will produce out-of-range values too.
	 * 
	 * @param perc
	 * @return value within the range
	 */
	public final double getAt(double perc) {
		return min + (max - min - MathUtils.EPS) * perc;
	}

	public double getCurrent() {
		return currValue;
	}

	public double getMedian() {
		return (min + max) * 0.5f;
	}

	public double getRange() {
		return max - min;
	}

	public boolean isValueInRange(float val) {
		return val >= min && val <= max;
	}

	public double pickRandom() {
		currValue = MathUtils.random(random, (float) min, (float) max);
		return currValue;
	}

	public DoubleRange seed(long seed) {
		random.setSeed(seed);
		return this;
	}

	public double setCurrent(double val) {
		currValue = MathUtils.clip(val, min, max);
		return currValue;
	}

	public DoubleRange setRandom(Random rnd) {
		random = rnd;
		return this;
	}

	public Double[] toArray(double step) {
		List<Double> range = new LinkedList<Double>();
		double v = min;
		while (v < max) {
			range.add(v);
			v += step;
		}
		return range.toArray(new Double[0]);
	}

	@Override
	public String toString() {
		return "DoubleRange: " + min + " -> " + max;
	}
}