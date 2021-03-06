package rekit.logic.gameelements;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import rekit.config.GameConf;
import rekit.logic.IScene;
import rekit.logic.gameelements.entities.pickups.DefaultCoin;
import rekit.logic.gameelements.inanimate.EndTrigger;
import rekit.logic.gameelements.inanimate.Inanimate;
import rekit.logic.gameelements.inanimate.InanimateBox;
import rekit.logic.gameelements.type.Boss;
import rekit.logic.gameelements.type.DynamicInanimate;
import rekit.logic.gameelements.type.Group;
import rekit.primitives.geometry.Vec;
import rekit.util.ReflectUtils;
import rekit.util.ThreadUtils;

/**
 *
 * This class will be used to load and group all GameElements.
 *
 */
public final class GameElementFactory {
	/**
	 * Prevent instantiation.
	 */
	private GameElementFactory() {
	}

	// Create connection to scene
	/**
	 * The current scene.
	 */
	private static IScene scene;
	/**
	 * Indicates whether the factory has been loaded.
	 */
	private static boolean loaded = false;

	/**
	 * Set the current scene.
	 *
	 * @param scene
	 *            the current scene.
	 */
	public static synchronized void setScene(IScene scene) {
		GameElementFactory.scene = scene;
	}

	/**
	 * All Groups.
	 */
	private static HashMap<String, GameElement[]> groups = new HashMap<>();
	/**
	 * All Elements.
	 */
	private static HashMap<String, GameElement> elements = new HashMap<>();

	/**
	 * Get Prototype by identifier.
	 *
	 * @param id
	 *            the identifier
	 * @return the GameElement or {@link InanimateBox} of none found.
	 */
	public static GameElement getPrototype(String id) {
		// Element
		GameElement element = GameElementFactory.elements.get(id);
		if (element != null) {
			return element;
		}
		// Group
		GameElement[] group = GameElementFactory.groups.get(id);
		if (group != null && group.length != 0) {
			return group[GameConf.PRNG.nextInt(group.length)];
		}

		GameConf.GAME_LOGGER.error("Error in GameElementFactory: Tried to get Prototype of GameElement with unknown ID " + id);
		// if none found --> inanimate
		return Inanimate.getPrototype();
	}

	/**
	 * Generate a new GameElement at position.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x pos
	 * @param y
	 *            the y pos
	 * @param modifiers
	 *            the optional modifiers
	 */
	public static void generate(String id, int x, int y, String... modifiers) {
		GameElement prototype = GameElementFactory.getPrototype(id);
		GameElementFactory.generate(prototype.create(new Vec(x, y), modifiers));

	}

	/**
	 * Generate a new GameElement.
	 *
	 * @param element
	 *            the element
	 */
	public static void generate(GameElement element) {
		if (element != null) {
			// Add GameElement to model
			GameElementFactory.scene.addGameElement(element);
		}

	}

	/**
	 * Initialize GameElementFactory.
	 */
	public static synchronized void initialize() {
		ThreadUtils.runDaemon("GameElementFactory", GameElementFactory::load);
	}

	/**
	 * Load the factory.
	 */
	private static synchronized void load() {
		if (GameElementFactory.loaded) {
			return;
		}
		GameElementFactory.loaded = true;
		GameElementFactory.loadElements();
		GameElementFactory.loadGroups();

	}

	/**
	 * Load all elements.
	 */
	private static void loadElements() {
		// Put Blocks in collection
		GameElementFactory.elements.put(Inanimate.getPrototype().getClass().getSimpleName(), Inanimate.getPrototype());
		GameElementFactory.elements.put(EndTrigger.getPrototype().getClass().getSimpleName(), EndTrigger.getPrototype());

		for (GameElement e : DynamicInanimate.getPrototypes()) {
			GameElementFactory.elements.put(e.getClass().getSimpleName(), e);
		}

		for (GameElement e : Boss.getPrototypes()) {
			GameElementFactory.elements.put(e.getClass().getSimpleName(), e);
		}
	}

	/**
	 * Load all groups.
	 */
	@SuppressWarnings("unchecked")
	private static void loadGroups() {
		for (Class<?> group : ReflectUtils.getClassesAnnotated(GameConf.SEARCH_PATH, Group.class)) {
			try {
				Group annotation = group.getAnnotation(Group.class);
				String name = annotation.value().isEmpty() ? group.getSimpleName() : annotation.value();
				GameConf.GAME_LOGGER.info("Loading group " + name);
				Method create = group.getDeclaredMethod("getPrototypes");
				create.setAccessible(true);
				GameElementFactory.createGroup((Set<? extends GameElement>) create.invoke(null), name);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				GameConf.GAME_LOGGER.error("Could not load prototypes of " + group.getSimpleName() + "\n" + e.getMessage());
			}
		}
	}

	/**
	 * Create a new group of Elements.
	 *
	 * @param prototypes
	 *            the prototypes
	 * @param name
	 *            the name of the group
	 */
	public static synchronized void createGroup(Set<? extends GameElement> prototypes, String name) {
		// Put Ts in collection and in separate array
		GameElement[] collection = new GameElement[prototypes.size()];
		int i = 0;
		for (GameElement e : prototypes) {
			GameElementFactory.elements.put(e.getClass().getSimpleName(), e);
			if (e.isAddableToGroup()) {
				collection[i++] = e;
			}
		}
		GameElement[] group = new GameElement[i];
		System.arraycopy(collection, 0, group, 0, i);
		GameElementFactory.groups.put(name, group);
	}

	/**
	 * Generate a {@link DefaultCoin} at position.
	 *
	 * @param x
	 *            the x pos
	 * @param y
	 *            the y pos
	 */
	public static void generateDefaultCoin(int x, int y) {
		GameElementFactory.generate(DefaultCoin.class.getSimpleName(), x, y);

	}

	/**
	 * Generate {@link Inanimate} at position.
	 *
	 * @param x
	 *            the x pos
	 * @param y
	 *            the y pos
	 */
	public static void generateInanimate(int x, int y) {
		GameElementFactory.generate(Inanimate.class.getSimpleName(), x, y);
	}
}
