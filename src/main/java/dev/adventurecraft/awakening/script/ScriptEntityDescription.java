package dev.adventurecraft.awakening.script;

public class ScriptEntityDescription {

	public int health = 10;
	public float height = 1.8F;
	public float width = 0.6F;
	public float moveSpeed = 0.7F;
	public String texture;
	public String onCreated = "";
	public String onUpdate = "";
	public String onAttacked = "";
	public String onPathReached = "";
	public String onDeath = "";
	public String onInteraction = "";

	public ScriptEntityDescription(String var1) {
		EntityDescriptions.addDescription(var1, this);
	}
}
