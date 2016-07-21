package edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.cannon;

import edu.kit.informatik.ragnarok.logic.Field;
import edu.kit.informatik.ragnarok.logic.gameelements.GameElement;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.Player;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.cannon.state.AimingState;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.cannon.state.CannonState;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.cannon.state.ChargingState;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.cannon.state.IdleState;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.cannon.state.ShootingState;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.particles.Particle;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.particles.ParticleSpawnerOption;
import edu.kit.informatik.ragnarok.logic.gameelements.inanimate.Inanimate;
import edu.kit.informatik.ragnarok.logic.gameelements.type.Enemy;
import edu.kit.informatik.ragnarok.primitives.geometry.Polygon;
import edu.kit.informatik.ragnarok.primitives.geometry.Vec;
import edu.kit.informatik.ragnarok.primitives.image.RGBColor;
import edu.kit.informatik.ragnarok.util.ReflectUtils.LoadMe;
import edu.kit.informatik.ragnarok.visitor.NoVisit;
import edu.kit.informatik.ragnarok.visitor.VisitInfo;
import edu.kit.informatik.ragnarok.visitor.Visitable;

/**
 * <p>
 * Enemy that resembles a cannon and periodically tries to shoot the
 * {@link Player} with a ray of ...laser? The Cannon itself cannot be killed,
 * however the {@link Player} can hide behind {@link Inanimate Inanimates} to
 * escape the laser.
 * </p>
 * <p>
 * The shooting consists of 4 time-based phases that are circularly repeated:
 * <ul>
 * <li><i>Idle</i>, where the Cannon aims directly downwards.</li>
 * <li><i>Aiming</i>, where the Cannon rotates to the {@link Player Players}
 * position with a fixed speed.</li>
 * <li><i>Charging</i>, where the rotation of the Cannon is frozed for a short
 * period of time an a graphical "shaking" starts and increases.</li>
 * <li><i>Shooting</i>, where the Cannon spawns {@link CannonParticle
 * CannonParticles} in a straight line.</li>
 * </ul>
 * </p>
 * <p>
 * Internally, it uses the {@link CannonStateMachine} and corresponding
 * {@link CannonState CannonStates} to implement the phase-like behavior as
 * described above.
 * </p>
 * 
 * @author Angelo Aracri
 */
@LoadMe
@VisitInfo(res = "conf/cannon", visit = true)
public class Cannon extends Enemy implements Visitable {

	/**
	 * Configurable Vector that holds the size of the Cannon.
	 */
	private static Vec SIZE;

	/**
	 * Configurable time in seconds, that the <i>idle</i> {@link CannonState} (
	 * {@link IdleState}) is going to take before switching to the next
	 * {@link CannonState}. before switching to the next state
	 */
	public static float STATE_IDLE_DURATION;

	/**
	 * Configurable time in seconds, that the <i>aiming</i> {@link CannonState}
	 * ( {@link AimingState}) is going to take before switching to the next
	 * {@link CannonState}. before switching to the next state
	 */
	public static float STATE_AIMING_DURATION;

	/**
	 * Configurable time in seconds, that the <i>charging</i>
	 * {@link CannonState} ( {@link ChargingState}) is going to take before
	 * switching to the next {@link CannonState}. before switching to the next
	 * state
	 */
	public static float STATE_CHARGING_DURATION;

	/**
	 * Configurable time in seconds, that the <i>shooting</i>
	 * {@link CannonState} ( {@link ShootingState}) is going to take before
	 * switching to the next {@link CannonState}. before switching to the next
	 * state
	 */
	public static float STATE_SHOOTING_DURATION;

	/**
	 * Configurable speed in radians per second that specify how fast the Cannon
	 * can rotate.
	 */
	public static float ANGLE_SPEED;

	/**
	 * Configurable {@link RGBColor} that will be used as the color of the base
	 * (the non-rotating part of the {@link Cannon}).
	 */
	public static RGBColor COLOR_BASE;

	/**
	 * Configurable {@link RGBColor} that will be used as the color of the pipe
	 * (the rotating part of the {@link Cannon}).
	 */
	public static RGBColor COLOR_CANNON;

	/**
	 * Configurable width of the {@link Cannon Cannons} pipe (the rotating part
	 * of the {@link Cannon}).
	 */
	public static float PIPE_W;

	/**
	 * Configurable height of the {@link Cannon Cannons} pipe (the rotating part
	 * of the {@link Cannon}).
	 */
	public static float PIPE_H;

	/**
	 * Configurable ratio of the rotating, inner circle to the non-rotating
	 * outer circle.
	 */
	public static float JOINT_RATIO;

	/**
	 * Configurable distance that defines the maximum shaking the pipe can do.
	 */
	public static float MAX_SHAKING;

	public static int PARTICLE_AMOUNT_MIN;
	public static int PARTICLE_AMOUNT_MAX;
	public static ParticleSpawnerOption PARTICLE_COLOR_R;
	public static ParticleSpawnerOption PARTICLE_COLOR_G;
	public static ParticleSpawnerOption PARTICLE_COLOR_B;
	public static ParticleSpawnerOption PARTICLE_COLOR_A;
	public static ParticleSpawnerOption PARTICLE_SPEED;
	public static float PARTICLE_TIME_MIN;
	public static float PARTICLE_TIME_MAX;

	/**
	 * Configurable average distance between the {@link Particle Particles}.
	 */
	public static float PARTICLE_DISTANCE_MU;

	/**
	 * Configurable sigma of the distance between the {@link Particle Particles}
	 * .
	 */
	public static float PARTICLE_DISTANCE_SIGMA;

	@NoVisit
	/**
	 * The inner, decorated {@link CannonStateMachine} that implements the phase-like behavior.
	 */
	private CannonStateMachine innerStateMachine;

	@NoVisit
	/**
	 * The angle in radians the {@link Cannon Cannons} pipe currently aims at, where 0 is down.
	 */
	private float currentAngle;

	@NoVisit
	/**
	 * The {@link Polygon} that will be used for rendering the {@link Cannon Cannons} pipe.
	 */
	private Polygon pipePolygon;

	/**
	 * Prototype constructor. Should not be used unless you know what you are
	 * doing.
	 */
	public Cannon() {
	}

	/**
	 * Main constructor that instantiates the inner {@link CannonStateMachine}
	 * and the {@link Polygon}.
	 * 
	 * @param pos
	 *            the position of the {@link Cannon}.
	 */
	public Cannon(Vec pos) {
		super(pos.addY(-0.5f + SIZE.getY() / 2f), new Vec(), SIZE);

		this.innerStateMachine = new CannonStateMachine(this, new IdleState());

		this.currentAngle = innerStateMachine.getState().getTargetAngle();

		this.pipePolygon = new Polygon(new Vec(), new Vec[] { new Vec(PIPE_W / 2, 0), new Vec(PIPE_W / 2, PIPE_H), new Vec(-PIPE_W / 2, PIPE_H),
				new Vec(-PIPE_W / 2, 0), new Vec(0, 0) });
	}

	@Override
	public GameElement create(Vec startPos, String[] options) {
		return new Cannon(startPos);
	}

	@Override
	public void internalRender(Field f) {

		// draw cannon base
		f.drawCircle(this.getPos(), this.getSize(), COLOR_BASE);
		f.drawRectangle(this.getPos().addY(-this.getSize().getY() / 4f), this.getSize().scalar(1, 0.5f), COLOR_BASE);

		// draw rotated cannon with (optional) shaking
		Vec cannonPos = this.getPos().addX(this.innerStateMachine.getState().getCannonShake());
		f.drawCircle(cannonPos, this.getSize().scalar(JOINT_RATIO), COLOR_CANNON);
		this.pipePolygon.moveTo(cannonPos);
		f.drawPolygon(this.pipePolygon.rotate(-this.currentAngle, this.getPos()), COLOR_CANNON, true);
	}

	@Override
	public void logicLoop(float deltaTime) {
		this.innerStateMachine.logicLoop(deltaTime);

		// move angle in right direction
		this.currentAngle += Math.signum(this.innerStateMachine.getState().getTargetAngle() - this.currentAngle) * deltaTime * ANGLE_SPEED;

		if (Math.abs(this.innerStateMachine.getState().getTargetAngle() - this.currentAngle) < ANGLE_SPEED / 20) {
			this.currentAngle = this.innerStateMachine.getState().getTargetAngle();
		}
	}

	/**
	 * Getter for the inner, decorated {@link CannonStateMachine}.
	 * 
	 * @return the inner {@link CannonStateMachine}.
	 */
	public CannonStateMachine getInnerStateMachine() {
		return this.innerStateMachine;
	}

	/**
	 * Signal that one of the {@link Particle Particles} collided with something
	 * and the laser should stop. Is only used while in the {@ShootingState
	 * 
	 * 
	 * 
	 * }.
	 */
	public void hitSomething() {
		this.innerStateMachine.getState().hitSomething();
	}

}