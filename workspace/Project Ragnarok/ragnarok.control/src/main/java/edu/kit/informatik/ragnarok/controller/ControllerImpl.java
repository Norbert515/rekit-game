package edu.kit.informatik.ragnarok.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.kit.informatik.ragnarok.controller.commands.Command;
import edu.kit.informatik.ragnarok.controller.commands.CommandSupervisor;
import edu.kit.informatik.ragnarok.controller.commands.FilterCommand;
import edu.kit.informatik.ragnarok.controller.commands.InputMethod;
import edu.kit.informatik.ragnarok.controller.commands.JumpCommand;
import edu.kit.informatik.ragnarok.controller.commands.MenuCommand;
import edu.kit.informatik.ragnarok.controller.commands.WalkCommand;
import edu.kit.informatik.ragnarok.gui.View;
import edu.kit.informatik.ragnarok.gui.filters.GrayScaleMode;
import edu.kit.informatik.ragnarok.gui.filters.InvertedMode;
import edu.kit.informatik.ragnarok.gui.filters.RandomMode;
import edu.kit.informatik.ragnarok.logic.GameState;
import edu.kit.informatik.ragnarok.logic.Model;
import edu.kit.informatik.ragnarok.logic.gameelements.entities.Entity;
import edu.kit.informatik.ragnarok.logic.gameelements.gui.menu.MenuItem;
import edu.kit.informatik.ragnarok.primitives.Direction;
import edu.kit.informatik.ragnarok.util.InputHelper;
import edu.kit.informatik.ragnarok.util.Tuple;

/**
 * This is an implementation of an {@link Controller} of the MVC <br>
 * It handles all input Events
 *
 * @author Dominik Fuchß
 *
 */
class ControllerImpl implements Observer, Controller, CommandSupervisor {
	/**
	 * Map State, Key-ID --> Command
	 */
	private Map<Tuple<GameState, Integer>, Command> mpCmd;
	/**
	 * The input helper
	 */
	private final InputHelperImpl helper;
	/**
	 * The model
	 */
	private Model model;

	/**
	 * Instantiate the Controller
	 *
	 * @param model
	 *            the model
	 */
	public ControllerImpl(Model model) {
		this.mpCmd = new HashMap<>();
		this.helper = new InputHelperImpl();
		this.model = model;
	}

	/**
	 * Initialize all commands
	 *
	 * @param view
	 *            the view
	 */
	private void init(View view) {
		// Menu
		this.mpCmd.put(Tuple.create(GameState.MENU, InputHelper.ESCAPE), new MenuCommand(this, MenuCommand.Dir.BACK));
		this.mpCmd.put(Tuple.create(GameState.MENU, InputHelper.ENTER), new MenuCommand(this, MenuCommand.Dir.SELECT));
		this.mpCmd.put(Tuple.create(GameState.MENU, InputHelper.ARROW_UP), new MenuCommand(this, MenuCommand.Dir.UP));
		this.mpCmd.put(Tuple.create(GameState.MENU, InputHelper.ARROW_DOWN), new MenuCommand(this, MenuCommand.Dir.DOWN));
		this.mpCmd.put(Tuple.create(GameState.MENU, InputHelper.ARROW_LEFT), new MenuCommand(this, MenuCommand.Dir.LEFT));
		this.mpCmd.put(Tuple.create(GameState.MENU, InputHelper.ARROW_RIGHT), new MenuCommand(this, MenuCommand.Dir.RIGHT));

		// Game
		this.mpCmd.put(Tuple.create(GameState.INGAME, InputHelper.ARROW_UP), new JumpCommand(this));
		this.mpCmd.put(Tuple.create(GameState.INGAME, InputHelper.ARROW_LEFT), new WalkCommand(this, Direction.LEFT));
		this.mpCmd.put(Tuple.create(GameState.INGAME, InputHelper.ARROW_RIGHT), new WalkCommand(this, Direction.RIGHT));

		// Filter Commands ... a test ('u', 'i', 'o' and 'p' key)
		this.mpCmd.put(Tuple.create(null, 117), new FilterCommand(true, view, new RandomMode()));
		this.mpCmd.put(Tuple.create(null, 105), new FilterCommand(true, view, new InvertedMode()));
		this.mpCmd.put(Tuple.create(null, 111), new FilterCommand(true, view, new GrayScaleMode()));
		this.mpCmd.put(Tuple.create(null, 112), new FilterCommand(false, view, null));

	}

	/**
	 * Handle one key input event
	 *
	 * @param id
	 *            the key's id
	 */
	public void handleEvent(int id, InputMethod inputMethod) {
		Tuple<GameState, Integer> key = Tuple.create(this.model.getState(), id);
		Tuple<GameState, Integer> idKey = Tuple.create(null, id);
		// return if we do not have a command defined for this key
		if (this.mpCmd.containsKey(key)) {
			this.mpCmd.get(key).execute(inputMethod);
			return;
		}
		if (this.mpCmd.containsKey(idKey)) {
			this.mpCmd.get(idKey).execute(inputMethod);
			return;
		}
		System.err.println("Warning: No Event defined for Key-ID: " + id + " State: " + this.model.getState());

	}

	@Override
	public void start(View view) {
		this.init(view);
		this.helper.initialize(view);
		this.helper.register(this);
	}

	@Override
	public void update() {
		Iterator<Integer> it = this.helper.getPressedKeyIterator();
		while (it.hasNext()) {
			this.handleEvent(it.next(), InputMethod.PRESS);
		}
		it = this.helper.getReleasedKeyIterator();
		while (it.hasNext()) {
			this.handleEvent(it.next(), InputMethod.RELEASE);
			it.remove();
		}
	}

	@Override
	public Entity getEntity(Command command) {
		return this.model.getPlayer();
	}

	@Override
	public MenuItem getMenu(Command command) {
		return this.model.getMenu();
	}
}
