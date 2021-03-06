package rekit.util;

import rekit.config.GameConf;
import rekit.primitives.geometry.Vec;

/**
 * This class contains several methods to calculate between units.
 *
 */
public final class CalcUtil {
	/**
	 * Prevent instantiation.
	 */
	private CalcUtil() {
	}

	/**
	 * Units to Pixels.
	 *
	 * @param units
	 *            the units
	 * @return the pixels
	 */
	public static int units2pixel(float units) {
		return (int) (units * GameConf.PX_PER_UNIT);
	}

	/**
	 * Units to Pixels.
	 *
	 * @param pos
	 *            the position
	 * @return the position in units
	 */
	public static Vec units2pixel(Vec pos) {
		return new Vec(pos.x * GameConf.PX_PER_UNIT, pos.y * GameConf.PX_PER_UNIT);
	}

	/**
	 * Randomize a value.
	 *
	 * @param mu
	 *            the value
	 * @param sigma
	 *            the sigma
	 * @return the randomized value
	 */
	public static double randomize(double mu, double sigma) {
		return mu + (GameConf.PRNG.nextDouble() * 2 - 1) * sigma;
	}

	/**
	 * Randomize a value.
	 *
	 * @param mu
	 *            the value
	 * @param sigma
	 *            the sigma
	 * @return the randomized value
	 */
	public static float randomize(float mu, float sigma) {
		return mu + (GameConf.PRNG.nextFloat() * 2 - 1) * sigma;
	}
}
