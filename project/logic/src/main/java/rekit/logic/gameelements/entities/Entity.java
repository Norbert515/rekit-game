package rekit.logic.gameelements.entities;

import rekit.config.GameConf;
import rekit.core.Team;
import rekit.logic.gameelements.GameElement;
import rekit.logic.gameelements.type.Enemy;
import rekit.logic.gameelements.type.Pickup;
import rekit.primitives.geometry.Direction;
import rekit.primitives.geometry.Frame;
import rekit.primitives.geometry.Vec;
import rekit.primitives.time.Timer;

/**
 * This class represents one of the most important {@link GameElement}-Type:<br>
 * These are the moving GameElements, which can interact between each other, so
 * as the {@link Player}, {@link Enemy Enemies}, {@link Pickup Pickups}, etc.
 *
 * @author Dominik Fuchss
 * @author Angelo Aracri
 *
 */
public abstract class Entity extends GameElement {

	/**
	 * The amount of lives the Entity has. Upon reaching 0 lives, he dies.
	 */
	protected int lives = 1;

	/**
	 * This {@link Timer} defines invincibility of an {@link Entity}.
	 * ({@code null} --&gt; not invincible)
	 */
	protected Timer invincibility = null;

	/**
	 * Minimal Constructor by {@link Team} used for prototype constructors. The
	 * element will not be initialized
	 *
	 * @param team
	 *            the team
	 */
	protected Entity(Team team) {
		super(team);
		this.setPos(new Vec());
		this.setSize(new Vec());
		this.setVel(new Vec());
	}

	/**
	 * Constructor that initializes attributes and takes a start position.
	 *
	 * @param startPos
	 *            the position this entity shall be in
	 * @param vel
	 *            the velocity
	 * @param size
	 *            the size
	 * @param team
	 *            the team
	 */
	protected Entity(Vec startPos, Vec vel, Vec size, Team team) {
		super(startPos, vel, size, team);
	}

	@Override
	public void addDamage(int damage) {
		// no damage taken while invincibility time is not up
		if (damage > 0 && this.invincibility != null && !this.invincibility.timeUp()) {
			return;
		}
		this.lives -= damage;
		if (damage > 0) {
			this.invincibility = new Timer(2000);
		}
		if (this.lives <= 0) {
			this.lives = 0;
			this.destroy();
		}
	}

	/**
	 * Set current lives.
	 *
	 * @param lives
	 *            the lives
	 */
	public final void setLives(int lives) {
		this.lives = lives;
	}

	@Override
	public final int getLives() {
		return this.lives;
	}

	@Override
	public final void logicLoop() {
		super.logicLoop();
		this.innerLogicLoop();
	}

	/**
	 * This method will calculate the next position of the Entity depending on
	 * the velocity.
	 */
	protected void innerLogicLoop() {
		if (this.invincibility != null) {
			this.invincibility.logicLoop();
		}

		// calculate new position
		// s1 = s0 + v*t because physics, thats why!
		this.setPos(this.getPos().add(this.getVel().scalar(this.deltaTime / 1000F)));

		Vec newVel = this.getVel();
		// apply gravity
		newVel = newVel.addY(GameConf.G);
		// apply slowing down walk
		newVel = newVel.addX(-Math.signum(newVel.x) * Player.STOP_ACCEL);
		// we don't want weird floating point velocities
		if (Math.abs(newVel.x) < 0.05) {
			newVel = newVel.setX(0);
		}
		// save new velocity
		this.setVel(newVel);
	}

	/**
	 * This implementation will ensure that no entity is able to fall through
	 * the ground or into another Object.
	 */
	@Override
	@Optional
	public void collidedWithSolid(Frame collision, Direction dir) {
		int signum = dir == Direction.LEFT || dir == Direction.UP ? -1 : 1;

		switch (dir) {
		case LEFT:
		case RIGHT:
			// move entities right side to collisions left side / vice versa
			float newX = collision.getBorder(dir) + signum * this.getSize().x / 1.9f;
			this.setPos(this.getPos().setX(newX));
			// stop velocity in x dimension
			this.setVel(this.getVel().setX(0));
			break;
		case UP:
		case DOWN:
			// move entities lower side to collisions top side / vice versa
			float newY = collision.getBorder(Direction.getOpposite(dir)) + signum * this.getSize().y / 1.9f;
			this.setPos(this.getPos().setY(newY));
			// stop velocity in y dimension
			this.setVel(this.getVel().setY(0));
			break;
		default:
			throw new Error();
		}
	}

	@Override
	protected boolean isVisible() {
		if (this.invincibility != null && !this.invincibility.timeUp()) {
			return (int) (this.invincibility.getProgress() * 20) % 2 == 0;
		}
		return super.isVisible();
	}

}
