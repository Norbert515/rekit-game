package ragnarok.logic.gameelements.entities;

import ragnarok.config.GameConf;
import ragnarok.core.GameElement;
import ragnarok.core.GameTime;
import ragnarok.core.Team;
import ragnarok.logic.gameelements.entities.state.DefaultState;
import ragnarok.logic.gameelements.entities.state.EntityState;
import ragnarok.logic.gameelements.entities.state.NotInitializedState;
import ragnarok.logic.gameelements.type.Enemy;
import ragnarok.logic.gameelements.type.Pickup;
import ragnarok.primitives.geometry.Direction;
import ragnarok.primitives.geometry.Frame;
import ragnarok.primitives.geometry.Vec;
import ragnarok.primitives.time.Timer;

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
	 * The amount of lifes the Entity has. Upon reaching 0 lifes, he dies.
	 */
	protected int lives = 1;

	/**
	 * The amount of points the Entity has scored.
	 */
	protected int points = 0;

	/**
	 * The current State the Entity is in and determines the jump behavior.
	 */
	protected EntityState entityState;
	/**
	 * This {@link Timer} defines invincibility of an {@link Entity}.
	 * ({@code null} --> not invincible)
	 */
	protected Timer invincibility = null;
	/**
	 * Last time of invoking {@link #logicLoop()}.
	 */
	private long lastTime = GameTime.getTime();
	/**
	 * The latest deltaTime in {@link #logicLoop()}.
	 */
	protected long deltaTime;

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
		this.setEntityState(new NotInitializedState(this));
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
		// Set to default state
		this.setEntityState(new DefaultState(this));
	}

	/**
	 * Set the Entities <i>EntitiyState</i> that determines its jump behavior.
	 *
	 * @param value
	 *            the new EntityState
	 */
	public final void setEntityState(EntityState value) {
		this.entityState = value;
	}

	/**
	 * Return the Entities current <i>EntitiyState</i> that determines its jump
	 * behavior.
	 *
	 * @return the state
	 */
	public final EntityState getEntityState() {
		return this.entityState;
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
	public final void addPoints(int points) {
		this.points += points;
	}

	@Override
	public final int getPoints() {
		return this.points;
	}

	@Override
	public final void logicLoop() {
		this.deltaTime = GameTime.getTime() - this.lastTime;
		this.lastTime += this.deltaTime;
		this.innerLogicLoop();
	}

	/**
	 * This method will calculate the next position of the Entity depending on
	 * the velocity.
	 */
	protected void innerLogicLoop() {
		// if delta is too big, clipping likely to appear...
		// if (deltaTime > GameConf.LOGIC_DELTA) {
		// // ..so recursively split work up into smaller parts
		// this.logicLoop(deltaTime / 2);
		// this.logicLoop(deltaTime / 2);
		// return;
		// }

		if (this.invincibility != null) {
			this.invincibility.logicLoop();
			// this.invincibility.removeTime(this.deltaTime);
		}

		this.getEntityState().logicLoop();

		// calculate new position
		// s1 = s0 + v*t because physics, thats why!
		this.setPos(this.getPos().add(this.getVel().scalar(this.deltaTime / 1000F)));

		Vec newVel = this.getVel();
		// apply gravity
		newVel = newVel.addY(GameConf.G);
		// apply slowing down walk
		newVel = newVel.addX(-Math.signum(newVel.getX()) * GameConf.PLAYER_STOP_ACCEL);
		// we dont want weird floating point velocities
		if (Math.abs(newVel.getX()) < 0.05) {
			newVel = newVel.setX(0);
		}
		// save new velocity
		this.setVel(newVel);
		// check if entity fell out of the world
		if (this.getPos().getY() > GameConf.GRID_H) {
			this.destroy();
		}
	}

	/**
	 * This implementation will ensure that no entity is able to fall through
	 * the ground or into another Object.
	 */
	@Override
	public void collidedWith(Frame collision, Direction dir) {
		// saving last position
		Vec lastPos = this.getLastPos();

		int signum = dir == Direction.LEFT || dir == Direction.UP ? -1 : 1;
		if (dir == Direction.UP) {
			this.getEntityState().floorCollision();
		}
		switch (dir) {
		case LEFT:
		case RIGHT:
			// move entities right side to collisions left side / vice versa
			float newX = collision.getBorder(dir) + signum * this.getSize().getX() / 1.9f;
			this.setPos(this.getPos().setX(newX));
			// stop velocity in x dimension
			this.setVel(this.getVel().setX(0));
			break;
		case UP:
		case DOWN:
			// move entities lower side to collisions top side / vice versa
			float newY = collision.getBorder(Direction.getOpposite(dir)) + signum * this.getSize().getY() / 1.9f;
			this.setPos(this.getPos().setY(newY));
			// stop velocity in y dimension
			this.setVel(this.getVel().setY(0));
			break;
		}

		// resetting lastPos
		this.setLastPos(lastPos);
	}

	/**
	 * By default this will return {@code 1}.
	 */
	@Override
	public byte getOrderZ() {
		return 1;
	}

	@Override
	protected boolean isVisible() {
		if (this.invincibility != null && !this.invincibility.timeUp()) {
			return (int) (this.invincibility.getProgress() * 20) % 2 == 0;
		}
		return super.isVisible();
	}

}