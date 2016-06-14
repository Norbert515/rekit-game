package edu.kit.informatik.ragnarok.logic.gameelements.entities;



public abstract class EntityState {
	private Entity entity;

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return this.entity;
	}

	public boolean canJump() {
		return true;
	}

	public abstract void render();

}
