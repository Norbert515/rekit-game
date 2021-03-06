package rekit.logic;

import rekit.logic.gameelements.entities.Player;
import rekit.logic.level.Level;

/**
 * This interface extends {@link IScene} and adds necessary methods to
 * encapsulate Levels.
 *
 * @author Matthias Schmitt
 *
 */
public interface ILevelScene extends IScene {
	/**
	 * Get the current player of {@code null} if none set.
	 *
	 * @return the current player of {@code null}
	 */
	Player getPlayer();

	/**
	 * Get the associated level.
	 *
	 * @return the level
	 */
	Level getLevel();

	/**
	 * End a level.
	 *
	 * @param won
	 *            indicates whether successful or died
	 */
	void end(boolean won);

	/**
	 * Indicates whether the level has ended.
	 *
	 * @return {@code true} if ended, {@code false} otherwise
	 */
	boolean hasEnded();

	/**
	 * This method indicates whether the offset is set to wildcard (any position
	 * is allowed / no elements will be deleted (because of their position))
	 *
	 * @return {@code true} if activated, {@code false} otherwise
	 */
	boolean isOffsetWildCard();

	/**
	 * Set return value of {@link #isOffsetWildCard()}.
	 * 
	 * @param wildcard
	 *            the value
	 */
	void setOffsetWildCard(boolean wildcard);

}
