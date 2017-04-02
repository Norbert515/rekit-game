package rekit.logic.scene;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import rekit.config.GameConf;
import rekit.logic.GameModel;
import rekit.logic.IScene;

/**
 *
 * This enum manages all different types of Scenes in the game.
 *
 */
public enum Scenes {
	/**
	 * A placeholder scene.
	 */
	NULL(NullScene.class, NullScene::create),
	/**
	 * A Menu.
	 */
	MENU(MainMenuScene.class, MainMenuScene::create),
	/**
	 * An infinite level.
	 */
	INFINITE(InfiniteLevelScene.class, InfiniteLevelScene::create),
	/**
	 * A level of the day.
	 */
	LOD(LevelOfTheDayScene.class, LevelOfTheDayScene::create),
	/**
	 * An arcade level.
	 */
	ARCADE(ArcadeLevelScene.class, ArcadeLevelScene::create),
	/**
	 * An boss rush level.
	 */
	BOSS_RUSH(BossRushScene.class, BossRushScene::create);
	/**
	 * The scene class.
	 */
	private Class<? extends Scene> clazz;
	/**
	 * The constructor of the scene.
	 */
	private final BiFunction<GameModel, String[], IScene> sceneConstructor;

	/**
	 * Create a scene type by class.
	 *
	 * @param sceneClass
	 *            the scene class
	 */
	Scenes(Class<? extends Scene> clazz, BiFunction<GameModel, String[], IScene> constructor) {
		this.clazz = clazz;
		this.sceneConstructor = constructor;
		if (ConcurrentHelper.INSTANCES.put(this.clazz, this) != null) {
			GameConf.GAME_LOGGER.warn("Multiple Scenes for class " + this.clazz);
		}
	}

	/**
	 * Get the corresponding instance of the scene.
	 *
	 * @param scene
	 *            the scene
	 * @return the Scenes Type
	 */
	public static Scenes getByInstance(IScene scene) {
		return ConcurrentHelper.INSTANCES.get(scene.getClass());
	}

	/**
	 * Indicates whether this scene is a menu.
	 *
	 * @return {@code true} if it is a menu, {@code false} otherwise
	 */
	public boolean isMenu() {
		return this.clazz.isAssignableFrom(MainMenuScene.class);
	}

	/**
	 * Get a new scene of a type.
	 *
	 * @param model
	 *            the model
	 * @param options
	 *            the options
	 * @return the scene
	 */
	public IScene getNewScene(GameModel model, String... options) {
		try {
			return this.sceneConstructor.apply(model, options);
		} catch (Exception e) {
			GameConf.GAME_LOGGER.fatal("Cannot load Scene for Level. Error: " + e.getMessage());
			return null;
		}

	}

	/**
	 * Static context in constructor of enums is quite difficult so we use a
	 * helper class.
	 *
	 * @author Dominik Fuchss
	 *
	 */
	private static class ConcurrentHelper {
		/**
		 * The mapping of IScene --&gt; Scenes.
		 */
		private static final ConcurrentHashMap<Class<? extends IScene>, Scenes> INSTANCES = new ConcurrentHashMap<>();
	}
}
