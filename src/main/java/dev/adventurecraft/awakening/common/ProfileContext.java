package dev.adventurecraft.awakening.common;

class ProfileContext {
	public String contextName;
	long startTime;

	public ProfileContext(String var1) {
		this.contextName = var1;
		this.startTime = System.nanoTime();
	}

	long getTime() {
		return System.nanoTime() - this.startTime;
	}
}
