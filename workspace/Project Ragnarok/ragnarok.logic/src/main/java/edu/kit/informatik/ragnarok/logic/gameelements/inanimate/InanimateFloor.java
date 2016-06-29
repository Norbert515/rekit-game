package edu.kit.informatik.ragnarok.logic.gameelements.inanimate;

import edu.kit.informatik.ragnarok.logic.gameelements.Field;
import edu.kit.informatik.ragnarok.logic.gameelements.GameElement;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.particles.ParticleSpawner;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.particles.ParticleSpawnerOption;
import edu.kit.informatik.ragnarok.primitives.Direction;
import edu.kit.informatik.ragnarok.primitives.Vec;
import edu.kit.informatik.ragnarok.util.RGBColor;

public class InanimateFloor extends Inanimate {

	private ParticleSpawner dustParticles;
	private static final ParticleSpawnerOption dustParticleAngleLeft = new ParticleSpawnerOption((float) ((7 / 4f) * Math.PI),
			(float) ((5 / 4f) * Math.PI), -(float) ((1 / 4f) * Math.PI), 0);
	private static final ParticleSpawnerOption dustParticleAngleRight = new ParticleSpawnerOption((float) ((1 / 4f) * Math.PI),
			(float) ((3 / 4f) * Math.PI), 0, (float) ((1 / 4f) * Math.PI));
	private static final ParticleSpawnerOption dustParticleAngleTop = new ParticleSpawnerOption((float) (-(1 / 2f) * Math.PI),
			(float) ((1 / 2f) * Math.PI), 0, 0);

	protected InanimateFloor(Vec pos, Vec size, RGBColor color) {
		super(pos, size, color);

		this.dustParticles = new ParticleSpawner();
		this.dustParticles.colorR = new ParticleSpawnerOption(this.color.red);
		this.dustParticles.colorG = new ParticleSpawnerOption(this.color.green);
		this.dustParticles.colorB = new ParticleSpawnerOption(this.color.blue);
		this.dustParticles.colorA = new ParticleSpawnerOption(255);

		this.dustParticles.amountMin = 8;
		this.dustParticles.amountMax = 15;

		this.dustParticles.speed = new ParticleSpawnerOption(2, 3, -1, 1);
	}

	@Override
	public void internalRender(Field f) {
		Vec pos = this.getPos();
		Vec size = this.getSize();

		f.drawRectangle(pos, size, this.color);

		RGBColor darkColor = new RGBColor(this.color.red - 30, this.color.green - 30, this.color.blue - 30);

		float plateThickness = 0.1f;
		f.drawRectangle(pos.add(new Vec(0, -size.getY() / 2f + plateThickness / 2f)), size.setY(plateThickness), darkColor);

	}

	@Override
	public void reactToCollision(GameElement element, Direction dir) {

		if (Math.abs(element.getVel().getY()) > 6) {
			this.dustParticles.angle = InanimateFloor.dustParticleAngleTop;
			this.dustParticles.spawn(this.scene, this.getPos().addY(-this.getSize().getY() / 2));
		}
		// if strong velocity in x direction
		else if (Math.abs(element.getVel().getX()) > 5) {
			// if moving right
			if (element.getVel().getX() > 0) {
				this.dustParticles.angle = InanimateFloor.dustParticleAngleLeft;
			} else {
				this.dustParticles.angle = InanimateFloor.dustParticleAngleRight;
			}

			Vec pos = this.getPos().addY(-this.getSize().getY() / 2).setX(element.getPos().getX());

			this.dustParticles.spawn(this.scene, pos);
		}

		super.reactToCollision(element, dir);
	}

	public static Inanimate staticCreate(Vec pos) {
		int randColG = (int) (Math.random() * 100 + 100);
		int randColRB = (int) (Math.random() * 40 + 30);
		return new InanimateFloor(pos, new Vec(1, 1), new RGBColor(randColRB, randColG, randColRB));
	}

}
