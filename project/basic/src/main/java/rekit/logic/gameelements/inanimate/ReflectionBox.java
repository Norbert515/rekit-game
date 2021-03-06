package rekit.logic.gameelements.inanimate;

import rekit.core.GameGrid;
import rekit.core.Team;
import rekit.logic.gameelements.GameElement;
import rekit.logic.gameelements.entities.Player;
import rekit.logic.gameelements.particles.ParticleSpawner;
import rekit.logic.gameelements.particles.ParticleSpawnerOption;
import rekit.logic.gameelements.type.DynamicInanimate;
import rekit.primitives.geometry.Direction;
import rekit.primitives.geometry.Vec;
import rekit.primitives.image.RGBAColor;
import rekit.util.ReflectUtils.LoadMe;

/**
 * This class realizes a Box which will "reflect" your {@link Player}.
 *
 */
@LoadMe
public final class ReflectionBox extends DynamicInanimate {
	/**
	 * The outer color.
	 */
	private static RGBAColor outerCol = new RGBAColor(100, 100, 100, 255);
	/**
	 * The inner color.
	 */
	private static RGBAColor innerCol = new RGBAColor(80, 140, 80, 255);
	/**
	 * The particles of this box.
	 */
	private static ParticleSpawner particles = null;

	static {
		ReflectionBox.particles = new ParticleSpawner();
		ReflectionBox.particles.angle = new ParticleSpawnerOption(0);
		ReflectionBox.particles.colorR = new ParticleSpawnerOption(ReflectionBox.innerCol.red);
		ReflectionBox.particles.colorG = new ParticleSpawnerOption(ReflectionBox.innerCol.green);
		ReflectionBox.particles.colorB = new ParticleSpawnerOption(ReflectionBox.innerCol.blue);
		ReflectionBox.particles.colorA = new ParticleSpawnerOption(0, 220);
		ReflectionBox.particles.timeMin = 0.2f;
		ReflectionBox.particles.timeMax = 0.4F;
		ReflectionBox.particles.amountMax = 1;
		ReflectionBox.particles.size = new ParticleSpawnerOption(0.3f, 0.5f, 0, 0);
		ReflectionBox.particles.speed = new ParticleSpawnerOption(2, 3, -1, 1);
	}

	/**
	 * Prototype Constructor.
	 */
	public ReflectionBox() {
		super();
	}

	/**
	 * Create a ReflectionBox.
	 *
	 * @param pos
	 *            the position
	 * @param size
	 *            the size
	 */
	protected ReflectionBox(Vec pos, Vec size) {
		super(pos, size, ReflectionBox.outerCol);
	}

	@Override
	public void reactToCollision(GameElement element, Direction dir) {
		if (element.getTeam() == Team.PLAYER) {
			// Calculate reflected velocity
			Vec v = element.getVel();
			if (dir == Direction.DOWN || dir == Direction.UP) {
				v = new Vec(1f * v.x, -0.8f * v.y);
			} else {
				v = new Vec(dir.getVector().x * Player.WALK_MAX_SPEED, 0.8f * Player.JUMP_BOOST);
			}

			// use internal collision
			super.reactToCollision(element, dir);

			// apply new velocity
			element.setVel(v);
		} else {
			super.reactToCollision(element, dir);

		}
	}

	@Override
	public void internalRender(GameGrid f) {
		Vec s = this.getSize();
		Vec p = this.getPos();

		f.drawRectangle(p, s, ReflectionBox.outerCol);
		f.drawRectangle(p.add(s.scalar(1 / 4f, 1 / 4f)), s.scalar(1 / 3f), ReflectionBox.innerCol);
		f.drawRectangle(p.add(s.scalar(1 / 4f, -1 / 4f)), s.scalar(1 / 3f), ReflectionBox.innerCol);
		f.drawRectangle(p.add(s.scalar(-1 / 4f, 1 / 4f)), s.scalar(1 / 3f), ReflectionBox.innerCol);
		f.drawRectangle(p.add(s.scalar(-1 / 4f, -1 / 4f)), s.scalar(1 / 3f), ReflectionBox.innerCol);
	}

	@Override
	public ReflectionBox create(Vec startPos, String... options) {
		return new ReflectionBox(startPos, new Vec(1, 1));
	}
}
