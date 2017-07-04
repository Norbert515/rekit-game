package rekit.logic.gameelements.entities.enemies;

import org.fuchss.configuration.Configurable;
import org.fuchss.configuration.annotations.NoSet;
import org.fuchss.configuration.annotations.SetterInfo;

import rekit.config.GameConf;
import rekit.core.GameGrid;
import rekit.logic.gameelements.GameElement;
import rekit.logic.gameelements.entities.Player;
import rekit.logic.gameelements.type.Enemy;
import rekit.primitives.geometry.Direction;
import rekit.primitives.geometry.Frame;
import rekit.primitives.geometry.Polygon;
import rekit.primitives.geometry.Vec;
import rekit.primitives.image.RGBAColor;
import rekit.util.ReflectUtils.LoadMe;

/**
 * <p>
 * Enemy that moves either horizontal or vertical and reflects upon colliding.
 * On each side it has either spikes or not, specified by the first 4 bits in
 * the int <i>sides</i>.
 * </p>
 * <p>
 * It has one life and gets damaged when touched by the Player on a side without
 * spikes, while damaging the Player if he touches a side with spikes.
 * </p>
 *
 * @author Angelo Aracri
 */
@SetterInfo(res = "conf/rektkiller")
@LoadMe
public final class RektKiller extends Enemy implements Configurable {

	/**
	 * The score the {@link Player} receives upon killing this {@link Enemy}
	 */
	public static int POINTS;

	/**
	 * Number whose first 4 bits are used as booleans for the spike at each
	 * side. First bit represents UP, the rest is clockwise.
	 */
	@NoSet
	private int sides;

	/**
	 * Cache the Polygon of the visualization of spikes that will later on be
	 * rotated and rendered.
	 */
	@NoSet
	private Polygon spikePolygon;

	/**
	 * Holds the Direction the RektKiller is currently moving to.
	 */
	@NoSet
	private Direction currentDirection;

	/**
	 * Prototype Constructor.
	 */
	public RektKiller() {
		super();
	}

	/**
	 * Standard Constructor that saves position, size and the integer that
	 * represents which sides have spikes.
	 *
	 * @param startPos
	 *            the initial position of the Enemy.
	 * @param size
	 *            the initial size of the Enemy.
	 * @param sides
	 *            the integer that represents which sides have sides.
	 */
	public RektKiller(Vec startPos, Vec size, int sides) {
		super(startPos, new Vec(), size);
		if (sides < 0 || sides > 15) {
			throw new IllegalArgumentException("RektKiller must be give a number between 0 and 14");
		}
		// save initial attributes
		int x = GameConf.PRNG.nextInt(Direction.values().length);
		this.setCurrentDirection(Direction.values()[x]);
		this.setSides(sides);

		this.prepare();
	}

	/**
	 * Alternative Constructor that uses the default size of (0.6, 0.6).
	 *
	 * @param startPos
	 *            the initial position of the Enemy
	 * @param sides
	 *            the integer that represents which sides have sides.
	 */
	public RektKiller(Vec startPos, int sides) {
		this(startPos, new Vec(0.6f, 0.6f), sides);
	}

	/**
	 * Calculates the Polygon for the size-dependent spikes and saves them in
	 * the attribute <i>spikePolygon</i>.
	 */
	public void prepare() {
		// calculate size dependent Polygon for spikes
		this.spikePolygon = new Polygon(new Vec(),
				new Vec[] { //
						new Vec(0.5f * ((this.getSize().x * 0.8f) / 3f), -(this.getSize().y * 0.8f) / 3f), new Vec(1.0f * ((this.getSize().x * 0.8f) / 3f), 0),
						new Vec(1.5f * ((this.getSize().x * 0.8f) / 3f), -(this.getSize().y * 0.8f) / 3f), new Vec(2.0f * ((this.getSize().x * 0.8f) / 3f), 0),
						new Vec(2.5f * ((this.getSize().x * 0.8f) / 3f), -(this.getSize().y * 0.8f) / 3f), new Vec(3.0f * ((this.getSize().x * 0.8f) / 3f), 0), //
						new Vec() //
		});
	}

	/**
	 * Checks whether the RektKiller has spikes on a given side.
	 *
	 * @param dir
	 *            the {@link Direction} that is checked.
	 * @return true if the RektKiller has spikes at the side <i>dir</i>, false
	 *         otherwise.
	 */
	public boolean hasSide(Direction dir) {
		int bitPos = dir.ordinal();
		return ((this.getSides() >> bitPos) & 1) == 1;
	}

	/**
	 * Sets if the RektKiller shall have spikes or not at a given side.
	 *
	 * @param dir
	 *            the {@link Direction} where to set/remove spikes to/from.
	 * @param spikes
	 *            true to set, false to remove spikes.
	 */
	public void setSide(Direction dir, boolean spikes) {
		int bitPos = dir.ordinal();
		if (spikes) {
			this.setSides(this.getSides() | (1 << bitPos));
		} else {
			this.setSides(this.getSides() & ~(1 << bitPos));
		}
	}

	@Override
	public void internalRender(GameGrid f) {
		RGBAColor innerColor = new RGBAColor(150, 30, 30);
		RGBAColor spikeColor = new RGBAColor(80, 80, 80);
		// draw rectangle in the middle
		f.drawRectangle(this.getPos(), this.getSize().scalar(0.8f), innerColor);
		// move to upper position
		this.spikePolygon.moveTo(this.getPos().add(this.getSize().scalar(-0.8f / 2f)));
		for (Direction d : Direction.values()) {
			if (this.hasSide(d)) {
				double angle = d.getAngle();
				Polygon rotatedSpikes = this.spikePolygon.rotate((float) angle, this.getPos());
				f.drawPolygon(rotatedSpikes, spikeColor, true);
			}
		}

	}

	@Override
	protected void innerLogicLoop() {
		// Do usual entity logic
		super.innerLogicLoop();

		if (this.getPos().y <= 0) {
			this.collidedWithSolid(new Frame(new Vec(0, 0), new Vec(0, 0)), Direction.DOWN);
		}
		if (this.getPos().y >= GameConf.GRID_H - 1) {
			this.collidedWithSolid(new Frame(new Vec(0, GameConf.GRID_H - 1), new Vec(0, GameConf.GRID_H - 1)), Direction.UP);
		}

		// We dont want this guy to fall
		this.setVel(this.getCurrentDirection().getVector().scalar(Player.WALK_MAX_SPEED));
	}

	@Override
	public void reactToCollision(GameElement element, Direction dir) {
		if (this.getTeam().isHostile(element.getTeam())) {
			// Touched harmless side
			if (!this.hasSide(dir)) {
				// give the player points
				this.getScene().getPlayer().addPoints(RektKiller.POINTS);
				// Let the player jump if he landed on top
				if (dir == Direction.UP) {
					element.killBoost();
				}
				// kill the enemy
				this.addDamage(1);
			} else {
				// Touched dangerous side
				// Give player damage
				element.addDamage(1);
				// Kill the enemy itself
				this.addDamage(1);
			}
		}
	}

	@Override
	public void collidedWithSolid(Frame collision, Direction dir) {
		super.collidedWithSolid(collision, dir);
		this.setCurrentDirection(Direction.getOpposite(this.getCurrentDirection()));
	}

	@Override
	public RektKiller create(Vec startPos, String... options) {
		RektKiller inst = new RektKiller(startPos, GameConf.PRNG.nextInt(16));

		// if option 0 is given: set defined direction
		if (options.length >= 1 && options[0] != null && options[0].matches("(\\+|-)?[0-9]+")) {
			int opt = Integer.parseInt(options[0]);
			if (opt >= 0 && opt < Direction.values().length) {
				inst.setCurrentDirection(Direction.values()[opt]);
			} else {
				GameConf.GAME_LOGGER.error("RektKiller was supplied invalid option " + options[0] + " at index 0 for Direction");
			}
		}

		// if option 1 is given: set defined sides
		if (options.length >= 2 && options[1] != null && options[1].matches("(\\+|-)?[0-9]+")) {
			inst.setSides(Integer.parseInt(options[1]));
		}

		return inst;
	}

	/**
	 * Getter for the current {@link Direction}, the RektKiller moves to.
	 *
	 * @return the current {@link Direction}, the RektKiller moves to.
	 */
	public Direction getCurrentDirection() {
		return this.currentDirection;
	}

	/**
	 * Setter for the current {@link Direction}, the RektKiller shall move to.
	 *
	 * @param currentDirection
	 *            the {@link Direction} the RektKiller shall move to.
	 */
	public void setCurrentDirection(Direction currentDirection) {
		this.currentDirection = currentDirection;
	}

	/**
	 * Getter for the number whose first 4 bits are used as booleans for the
	 * spikes at each side.
	 *
	 * @return the int <i>sides</i> that represents spike positions.
	 */
	public int getSides() {
		return this.sides;
	}

	/**
	 * Setter for the number whose first 4 bits are used as booleans for the
	 * spikes at each side.
	 *
	 * @param sides
	 *            the int <i>sides</i> that represents spike positions.
	 */
	public void setSides(int sides) {
		this.sides = sides;
	}

}
