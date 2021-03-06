package rekit.logic.gameelements.inanimate;

import rekit.config.GameConf;
import rekit.core.GameGrid;
import rekit.logic.gameelements.GameElement;
import rekit.logic.gameelements.particles.Particle;
import rekit.logic.gameelements.particles.ParticleSpawner;
import rekit.logic.gameelements.particles.ParticleSpawnerOption;
import rekit.primitives.geometry.Direction;
import rekit.primitives.geometry.Vec;
import rekit.primitives.image.RGBAColor;
import rekit.primitives.operable.OpProgress;

/**
 * The standard Inanimate that will be used for the lowest height in the level.
 * By mechanics, it does not differ from the {@link InanimateBox} but is instead
 * designed to resemble grass.
 *
 * @author Angelo Aracri
 */
public class InanimateFloor extends Inanimate {

	/**
	 * The amount of layers that the {@link InanimateFloor} should be composed
	 * of.
	 */
	private static int LAYERS = 5;

	/**
	 * The amount of GrassStraws the InanimateFloor shall have.
	 */
	private static int STRAW_NUM = 20;

	/**
	 * Holds the colors for each layer the {@link InanimateFloor} is composed
	 * of.
	 */
	private static RGBAColor[] layerCols;

	/**
	 * Holds the positions relative to the middle position for each layer the
	 * {@link InanimateFloor} is composed of.
	 */
	private static Vec[] layerVecs;

	/**
	 * Holds all GrassStraws that will be rendered on the upper half of the
	 * {@link InanimateFloor}.
	 */
	private GrassStraw[] straws;

	/**
	 * Cached version of the {@link ParticleSpawner} that will be used to show
	 * both {@link Particle Particles} when an {@link Entity} walks on the
	 * surface very fast and when an {@link Entity} lands on it very fast.
	 */
	private static ParticleSpawner dustParticles;

	/**
	 * Holds the angle for the {@link ParticleSpawner} that is used when an
	 * {@link Entity} walks fast to the right on the {@link InanimateFloor}.
	 */
	private static final ParticleSpawnerOption DUST_PARTICLE_ANGLE_LEFT;

	/**
	 * Holds the angle for the {@link ParticleSpawner} that is used when an
	 * {@link Entity} walks fast to the left on the {@link InanimateFloor}.
	 */
	private static final ParticleSpawnerOption DUST_PARTICLE_ANGLE_RIGHT;

	/**
	 * Holds the angle for the {@link ParticleSpawner} that is used when an
	 * {@link Entity} lands on the {@link InanimateFloor}.
	 */
	private static final ParticleSpawnerOption DUST_PARTICLE_ANGLE_TOP;
	// TODO: Config file.
	static {
		// define color range for blocks layers
		RGBAColor startCol = new RGBAColor(22, 140, 30);
		RGBAColor endCol = new RGBAColor(23, 210, 49);

		// define position range for blocks layers
		float size = 1;
		Vec startVec = new Vec(0, -size / 2 + (size / 2) / InanimateFloor.LAYERS);
		Vec endVec = new Vec(0, size / 2 - (size / 2) / InanimateFloor.LAYERS);

		// create Progresses from ranges.
		OpProgress<RGBAColor> progCols = new OpProgress<>(startCol, endCol);
		OpProgress<Vec> progVecs = new OpProgress<>(startVec, endVec);

		// fill arrays for each layer
		InanimateFloor.layerCols = new RGBAColor[InanimateFloor.LAYERS];
		InanimateFloor.layerVecs = new Vec[InanimateFloor.LAYERS];
		for (int i = 0; i < InanimateFloor.LAYERS; i++) {
			float prog = i / (float) (InanimateFloor.LAYERS - 1);
			InanimateFloor.layerCols[i] = progCols.getNow(prog);
			InanimateFloor.layerVecs[i] = progVecs.getNow(prog);
		}

		// prepare particles
		InanimateFloor.dustParticles = new ParticleSpawner();
		InanimateFloor.dustParticles.colorR = new ParticleSpawnerOption(InanimateFloor.layerCols[0].red);
		InanimateFloor.dustParticles.colorG = new ParticleSpawnerOption(InanimateFloor.layerCols[0].green);
		InanimateFloor.dustParticles.colorB = new ParticleSpawnerOption(InanimateFloor.layerCols[0].blue);
		InanimateFloor.dustParticles.colorA = new ParticleSpawnerOption(255);
		DUST_PARTICLE_ANGLE_LEFT = new ParticleSpawnerOption((float) ((7 / 4f) * Math.PI), (float) ((5 / 4f) * Math.PI),
				-(float) ((1 / 4f) * Math.PI), 0);
		DUST_PARTICLE_ANGLE_RIGHT = new ParticleSpawnerOption((float) ((1 / 4f) * Math.PI),
				(float) ((3 / 4f) * Math.PI), 0, (float) ((1 / 4f) * Math.PI));
		DUST_PARTICLE_ANGLE_TOP = new ParticleSpawnerOption((float) (-(1 / 2f) * Math.PI), (float) ((1 / 2f) * Math.PI),
				0, 0);
		InanimateFloor.dustParticles.amountMin = 8;
		InanimateFloor.dustParticles.amountMax = 15;

		InanimateFloor.dustParticles.speed = new ParticleSpawnerOption(2, 3, -1, 1);
	}

	/**
	 * <p>
	 * Standard constructor that requires position and size of the
	 * {@link InanimateFloor}.
	 * </p>
	 * <p>
	 * Also, it creates the {@link GrassStraw GrassStraws} that will be rendered
	 * on top of the {@link InanimateFloor}.
	 * </p>
	 *
	 * @param pos
	 *            the position of the {@link InanimateFloor}.
	 * @param size
	 *            the size of the {@link InanimateFloor}.
	 */
	protected InanimateFloor(Vec pos, Vec size) {
		super(pos, size, InanimateFloor.layerCols[0]);

		this.straws = new GrassStraw[InanimateFloor.STRAW_NUM];
		for (int i = 0; i < InanimateFloor.STRAW_NUM; i++) {
			this.straws[i] = new GrassStraw(pos);
		}
	}

	/**
	 * <p>
	 * Internal class whose only purpose is to represent a graphical element. It
	 * is supposed to look like a GrassStraw.
	 * </p>
	 * <p>
	 * It must be supplied with the parent position but alters the position as
	 * well as its size and color to create a random effect.
	 * </p>
	 * <p>
	 * In order to show the {@link GrassStraw} one must call
	 * {@link #internalRender(GameGrid f)}.
	 * </p>
	 *
	 * @author Angelo Aracri
	 */
	private static class GrassStraw {
		/**
		 * The (absolute) position of the {@link GrassStraw}.
		 */
		private Vec pos;

		/**
		 * The color of the {@link GrassStraw}.
		 */
		private RGBAColor col;

		/**
		 * The size of the {@link GrassStraw}.
		 */
		private Vec size;

		/**
		 * Standard constructor that must be supplied with the position of the
		 * parenting {@link InanimateFloor} and performs all random calculations
		 * (positioning, size, color).
		 *
		 * @param parentPos
		 *            the position of the parenting {@link InanimateFloor}.
		 */
		GrassStraw(Vec parentPos) {
			this.pos = parentPos
					.add(new Vec(GameConf.PRNG.nextFloat() * 0.9f - 0.45f, -0.3f - GameConf.PRNG.nextFloat() * 0.4f));
			this.size = new Vec(0.06f, 0.3f + GameConf.PRNG.nextFloat() * 0.4f);
			this.col = InanimateFloor.layerCols[GameConf.PRNG.nextInt(InanimateFloor.layerCols.length)];
		}

		/**
		 * Render method to display the {@link GrassStraw}.
		 *
		 * @param f
		 *            the {@link GameGrid} to render upon.
		 */
		public void internalRender(GameGrid f) {
			f.drawRectangle(this.pos, this.size, this.col);
		}
	}

	@Override
	public void internalRender(GameGrid f) {
		// Draw rectangles that this Floor is composed of
		for (int i = 0; i < InanimateFloor.LAYERS; i++) {
			f.drawRectangle(this.getPos().add(InanimateFloor.layerVecs[i]),
					this.getSize().setY(1 / (float) InanimateFloor.LAYERS), InanimateFloor.layerCols[i]);
		}
		// Draw GrassStraws
		for (GrassStraw straw : this.straws) {
			straw.internalRender(f);
		}
	}

	@Override
	public void reactToCollision(GameElement element, Direction dir) {

		if (Math.abs(element.getVel().y) > 6) {
			InanimateFloor.dustParticles.angle = InanimateFloor.DUST_PARTICLE_ANGLE_TOP;
			InanimateFloor.dustParticles.spawn(this.getScene(), this.getPos().addY(-this.getSize().y / 2));
		} else if (Math.abs(element.getVel().x) > 5) {
			// if strong velocity in x direction

			// if moving right
			if (element.getVel().x > 0) {
				InanimateFloor.dustParticles.angle = InanimateFloor.DUST_PARTICLE_ANGLE_LEFT;
			} else {
				InanimateFloor.dustParticles.angle = InanimateFloor.DUST_PARTICLE_ANGLE_RIGHT;
			}

			Vec pos = this.getPos().addY(-this.getSize().y / 2).setX(element.getPos().x);

			InanimateFloor.dustParticles.spawn(this.getScene(), pos);
		}

		super.reactToCollision(element, dir);
	}

	/**
	 * Static create method that will be used by {@link Inanimate} for it in
	 * order to decide between {@link InanimateFloor} and {@link InanimateBox}.
	 *
	 * @param pos
	 *            the position to create the {@link InanimateFloor} to.
	 * @return the newly created {@link InanimateFloor}.
	 */
	public static Inanimate staticCreate(Vec pos) {
		return new InanimateFloor(pos, new Vec(1, 1));
	}

}
