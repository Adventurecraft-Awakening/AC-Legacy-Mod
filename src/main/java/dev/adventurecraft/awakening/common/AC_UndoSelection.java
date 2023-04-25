package dev.adventurecraft.awakening.common;

public class AC_UndoSelection {
	public AC_Selection before = new AC_Selection();
	public AC_Selection after = new AC_Selection();

	public AC_UndoSelection() {
		this.before.record();
	}
}
