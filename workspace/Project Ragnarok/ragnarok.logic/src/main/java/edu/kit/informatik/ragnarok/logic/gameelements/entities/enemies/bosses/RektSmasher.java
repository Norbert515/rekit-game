package edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.bosses;

import edu.kit.informatik.ragnarok.config.GameConf;
import edu.kit.informatik.ragnarok.logic.gameelements.Field;
import edu.kit.informatik.ragnarok.logic.gameelements.GameElement;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.Entity;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.RektKiller;
import edu.kit.informatik.ragnarok.primitives.Direction;
import edu.kit.informatik.ragnarok.primitives.Frame;
import edu.kit.informatik.ragnarok.primitives.Vec2D;

public class RektSmasher extends Boss {

	private RektKiller innerRektKiller;

	public RektSmasher(Vec2D startPos) {

		// Configure own attributes
		super(startPos);
		this.setSize(new Vec2D(2f, 2f));

		// Configure innerRektKiller
		this.innerRektKiller = new RektKiller(startPos, 15);
		this.innerRektKiller.setCurrentDirection(Direction.DOWN);
		this.innerRektKiller.setSize(this.getSize());
		this.innerRektKiller.prepare();

		this.setLifes(3);

	}

	private float speed = 0.5f;

	@Override
	public void addDamage(int damage) {
		super.addDamage(damage);
		this.speed = 0.5f + (3 - this.getLifes()) * 0.25f;
	}

	@Override
	public void render(Field f) {

		// Update innerRektKiller
		this.innerRektKiller.setPos(this.getPos());

		// Render innerRektKiller
		this.innerRektKiller.render(f);

		// Add face image above regular innerRektKiller visualization
		int lifes = this.getLifes() > 3 ? 3 : this.getLifes();
		f.drawImage(this.getPos(), this.getSize().multiply(0.8f),
				"rektSmasher_" + lifes + ".png");
	}

	@Override
	public void collidedWith(Frame collision, final Direction dir) {

		Vec2D dif = this.getPos().add(getTarget().getPos().multiply(-1));

		super.collidedWith(collision, dir);

		Direction newDir;

		// if (Math.abs(dif.getX()) / GameConf.gridW > Math.abs(dif.getY()) /
		// GameConf.gridH) {
		if (dir == Direction.UP || dir == Direction.DOWN) {
			if (dif.getX() > 0) {
				newDir = Direction.LEFT;
			} else {
				newDir = Direction.RIGHT;
			}
		} else {
			if (dif.getY() > 0) {
				newDir = Direction.UP;
			} else {
				newDir = Direction.DOWN;
			}
		}

		while (this.innerRektKiller.getCurrentDirection() == newDir) {
			newDir = Direction.values()[(int) (Math.random() * Direction
					.values().length)];
		}
		this.innerRektKiller.setCurrentDirection(newDir);

		if (Math.random() > 0.9) {
			this.innerRektKiller.setSide(dir.getOpposite(), false);
			Thread spikeRespawnThread = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					RektSmasher.this.innerRektKiller.setSide(dir.getOpposite(),
							true);
				}
			};

			spikeRespawnThread.start();
		}
	}

	@Override
	public void reactToCollision(GameElement element, Direction dir) {
		if (this.isHarmless()) {
			return;
		}

		if (this.isHostile(element)) {
			// Touched harmless side
			if (!this.innerRektKiller.hasSide(dir)) {
				// Let the player jump if he landed on top
				if (dir == Direction.UP) {
					element.setVel(element.getVel().setY(
							GameConf.PLAYER_JUMP_BOOST));
				}

				// kill the enemy
				this.addDamage(1);

			}
			// Touched dangerous side
			else {
				// Give player damage
				element.addDamage(1);
			}
		}

	}

	@Override
	public void logicLoop(float deltaTime) {
		// if no invincibility or invincibility time is up
		if (this.invincibility == null
				|| (this.invincibility != null && this.invincibility.timeUp())) {
			this.setHarmless(false);
		}
		// if invincible
		if (this.invincibility != null && !this.invincibility.timeUp()) {
			this.setHarmless(true);
		}
		// we dont want him damaging the player when hes actually dead
		if (this.getLifes() <= 0) {
			this.setHarmless(true);
		}

		this.setVel(this.innerRektKiller.getCurrentDirection().getVector()
				.multiply(this.speed * GameConf.PLAYER_WALK_MAX_SPEED));

		super.logicLoop(deltaTime);
	}

	@Override
	public String getName() {
		return "RektSmasher";
	}

	@Override
	public Entity create(Vec2D startPos) {
		RektSmasher clone = new RektSmasher(startPos);
		clone.setTarget(this.getTarget());
		clone.setBossRoom(this.getBossRoom());
		return clone;
	}

}