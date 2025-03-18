package dev.adventurecraft.awakening.tile.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityWeather extends TileEntity {
	public boolean changePrecipitate;
	public boolean precipitate;
	public boolean changeTempOffset;
	public double tempOffset;
	public boolean changeTimeOfDay;
	public int timeOfDay;
	public boolean changeTimeRate;
	public float timeRate;
	public boolean thundering;
	public boolean changeThundering;

	public void load(CompoundTag var1) {
		super.load(var1);
		this.changePrecipitate = var1.getBoolean("changePrecipitate");
		this.precipitate = var1.getBoolean("precipitate");
		this.changeTempOffset = var1.getBoolean("changeTempOffset");
		this.tempOffset = var1.getDouble("tempOffset");
		this.changeTimeOfDay = var1.getBoolean("changeTimeOfDay");
		this.timeOfDay = var1.getInt("timeOfDay");
		this.changeTimeRate = var1.getBoolean("changeTimeRate");
		this.timeRate = var1.getFloat("timeRate");
		this.changeThundering = var1.getBoolean("changeThundering");
		this.thundering = var1.getBoolean("thundering");
	}

	public void save(CompoundTag var1) {
		super.save(var1);
		var1.putBoolean("changePrecipitate", this.changePrecipitate);
		var1.putBoolean("precipitate", this.precipitate);
		var1.putBoolean("changeTempOffset", this.changeTempOffset);
		var1.putDouble("tempOffset", this.tempOffset);
		var1.putBoolean("changeTimeOfDay", this.changeTimeOfDay);
		var1.putInt("timeOfDay", this.timeOfDay);
		var1.putBoolean("changeTimeRate", this.changeTimeRate);
		var1.putFloat("timeRate", this.timeRate);
		var1.putBoolean("changeThundering", this.changeThundering);
		var1.putBoolean("thundering", this.thundering);
	}
}
