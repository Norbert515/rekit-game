package rekit.core;

/**
 * This interface defines an renderer, which can draw on a {@link GameGrid}.
 *
 * @author Dominik Fuchss
 *
 */
@FunctionalInterface
public interface Renderer {
	/**
	 * Draw on a field.
	 *
	 * @param f
	 *            the field
	 */
	void render(GameGrid f);
}