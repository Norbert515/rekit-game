package edu.kit.informatik.ragnarok.logic.gameelements.entities.pickups;

import edu.kit.informatik.ragnarok.logic.gameelements.type.Coin;
import edu.kit.informatik.ragnarok.logic.gameelements.type.Pickup;
import edu.kit.informatik.ragnarok.primitives.geometry.Vec;
import edu.kit.informatik.ragnarok.primitives.image.RGBAColor;
import edu.kit.informatik.ragnarok.util.ReflectUtils.LoadMe;

/**
 * This class defines a simple {@link Pickup}; a <b>GhostCoin</b> which will
 * give the player points and is nearly invisible.
 *
 * @author Dominik Fuchss
 *
 */
@LoadMe
public final class GhostCoin extends Coin {
	/**
	 * The default color of the coin.
	 */
	private static RGBAColor color = new RGBAColor(30, 100, 216, 7);
	/**
	 * The shadow color of the coin.
	 */
	private static RGBAColor darkColor = new RGBAColor(50, 25, 156, 7);

	/**
	 * Prototype constructor.
	 */
	public GhostCoin() {
		super();
	}

	/**
	 * Constructor with position.
	 *
	 * @param startPos
	 *            the position.
	 */
	protected GhostCoin(Vec startPos) {
		super(startPos);
	}

	@Override
	protected RGBAColor getColor() {
		return GhostCoin.color;
	}

	@Override
	protected RGBAColor getDarkerColor() {
		return GhostCoin.darkColor;
	}

	@Override
	public Coin create(Vec startPos, String[] options) {
		return new GhostCoin(startPos);
	}

	@Override
	protected int getValue() {
		return 30;
	}

}
