package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityWeather extends BlockEntity {
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

	public void readNBT(CompoundTag var1) {
		super.readNBT(var1);
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

	public void writeNBT(CompoundTag var1) {
		super.writeNBT(var1);
		var1.put("changePrecipitate", this.changePrecipitate);
		var1.put("precipitate", this.precipitate);
		var1.put("changeTempOffset", this.changeTempOffset);
		var1.put("tempOffset", this.tempOffset);
		var1.put("changeTimeOfDay", this.changeTimeOfDay);
		var1.put("timeOfDay", this.timeOfDay);
		var1.put("changeTimeRate", this.changeTimeRate);
		var1.put("timeRate", this.timeRate);
		var1.put("changeThundering", this.changeThundering);
		var1.put("thundering", this.thundering);
	}
}
