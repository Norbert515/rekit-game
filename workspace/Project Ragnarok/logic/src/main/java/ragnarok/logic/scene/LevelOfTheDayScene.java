package ragnarok.logic.scene;

import ragnarok.logic.GameModel;
import ragnarok.logic.level.LevelManager;

/**
 * This class realizes a LevelScene for lotd levels.
 */
final class LevelOfTheDayScene extends LevelScene {
	/**
	 * Create a new LOTD Scene.
	 * 
	 * @param model
	 *            the model
	 */
	public LevelOfTheDayScene(GameModel model) {
		super(model, LevelManager.getLOTDLevel());
	}

	/**
	 * Create method of the scene.
	 *
	 * @param model
	 *            the model
	 * @param options
	 *            the options
	 * @return a new arcade scene.
	 */
	public static Scene create(GameModel model, String[] options) {
		return new LevelOfTheDayScene(model);
	}

}