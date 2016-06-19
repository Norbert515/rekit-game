package edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.bosses;

import edu.kit.informatik.ragnarok.logic.gameelements.entities.enemies.RektKiller;
import edu.kit.informatik.ragnarok.logic.gameelements.inanimate.Inanimate;
import edu.kit.informatik.ragnarok.logic.levelcreator.BossRoom;
import edu.kit.informatik.ragnarok.primitives.Vec2D;

public class RektSmasher extends RektKiller {

	private BossRoom bossRoom;
	
	public RektSmasher(Vec2D startPos, BossRoom bossRoom) {
		super(startPos, 1);
		this.setSize(new Vec2D(3, 3));
		this.bossRoom = bossRoom;
	}
	
	public void destroy() {
		super.destroy();
		bossRoom.endBattle();
	}
}