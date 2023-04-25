package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityEffect extends BlockEntity {
    public boolean checkTrigger = true;
    public String particleType = "heart";
    public int particlesPerSpawn = 1;
    public int ticksBetweenParticles = 1;
    public boolean isActivated = false;
    public int ticksBeforeParticle = 0;
    public float offsetX = 0.0F;
    public float offsetY = 0.0F;
    public float offsetZ = 0.0F;
    public float randX = 0.0F;
    public float randY = 0.0F;
    public float randZ = 0.0F;
    public float floatArg1 = 0.0F;
    public float floatArg2 = 0.0F;
    public float floatArg3 = 0.0F;
    public float floatRand1 = 0.0F;
    public float floatRand2 = 0.0F;
    public float floatRand3 = 0.0F;
    public int changeFogColor;
    public float fogR;
    public float fogG;
    public float fogB;
    public int changeFogDensity;
    public float fogStart;
    public float fogEnd;
    public boolean setOverlay = false;
    public String overlay = "";
    public boolean revertTextures = false;
    public boolean replaceTextures = false;
    public String textureReplacement = "";
    private static Random rand = new Random();

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.particleType = var1.getString("particleType");
        this.particlesPerSpawn = var1.getInt("particlesPerSpawn");
        this.ticksBetweenParticles = var1.getInt("ticksBetweenParticles");
        this.isActivated = var1.getBoolean("isActivated");
        this.offsetX = var1.getFloat("offsetX");
        this.offsetY = var1.getFloat("offsetY");
        this.offsetZ = var1.getFloat("offsetZ");
        this.randX = var1.getFloat("randX");
        this.randY = var1.getFloat("randY");
        this.randZ = var1.getFloat("randZ");
        this.floatArg1 = var1.getFloat("floatArg1");
        this.floatArg2 = var1.getFloat("floatArg2");
        this.floatArg3 = var1.getFloat("floatArg3");
        this.floatRand1 = var1.getFloat("floatRand1");
        this.floatRand2 = var1.getFloat("floatRand2");
        this.floatRand3 = var1.getFloat("floatRand3");
        this.changeFogColor = var1.getInt("changeFogColor");
        this.fogR = var1.getFloat("fogR");
        this.fogG = var1.getFloat("fogG");
        this.fogB = var1.getFloat("fogB");
        this.changeFogDensity = var1.getInt("changeFogDensity");
        this.fogStart = var1.getFloat("fogStart");
        this.fogEnd = var1.getFloat("fogEnd");
        this.setOverlay = var1.getBoolean("setOverlay");
        this.overlay = var1.getString("overlay");
        this.revertTextures = var1.getBoolean("revertTextures");
        this.replaceTextures = var1.getBoolean("replaceTextures");
        this.textureReplacement = var1.getString("textureReplacement");
    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        if (!this.particleType.equals("")) {
            var1.put("particleType", this.particleType);
        }

        var1.put("particlesPerSpawn", this.particlesPerSpawn);
        var1.put("ticksBetweenParticles", this.ticksBetweenParticles);
        var1.put("isActivated", this.isActivated);
        var1.put("offsetX", this.offsetX);
        var1.put("offsetY", this.offsetY);
        var1.put("offsetZ", this.offsetZ);
        var1.put("randX", this.randX);
        var1.put("randY", this.randY);
        var1.put("randZ", this.randZ);
        var1.put("floatArg1", this.floatArg1);
        var1.put("floatArg2", this.floatArg2);
        var1.put("floatArg3", this.floatArg3);
        var1.put("floatRand1", this.floatRand1);
        var1.put("floatRand2", this.floatRand2);
        var1.put("floatRand3", this.floatRand3);
        var1.put("changeFogColor", this.changeFogColor);
        var1.put("fogR", this.fogR);
        var1.put("fogG", this.fogG);
        var1.put("fogB", this.fogB);
        var1.put("changeFogDensity", this.changeFogDensity);
        var1.put("fogStart", this.fogStart);
        var1.put("fogEnd", this.fogEnd);
        var1.put("setOverlay", this.setOverlay);
        var1.put("overlay", this.overlay);
        var1.put("revertTextures", this.revertTextures);
        var1.put("replaceTextures", this.replaceTextures);
        var1.put("textureReplacement", this.textureReplacement);
    }

    public void tick() {
        if (this.checkTrigger) {
            this.isActivated = ((ExWorld) this.world).getTriggerManager().isActivated(this.x, this.y, this.z);
            this.checkTrigger = false;
        }

        if (this.isActivated) {
            if (this.ticksBeforeParticle > 0) {
                --this.ticksBeforeParticle;
            } else {
                for (int var1 = 0; var1 < this.particlesPerSpawn; ++var1) {
                    this.world.addParticle(this.particleType, (double) this.x + 0.5D + (double) this.randX * (2.0D * rand.nextDouble() - 1.0D) + (double) this.offsetX, (double) this.y + 0.5D + (double) this.randY * (2.0D * rand.nextDouble() - 1.0D) + (double) this.offsetY, (double) this.z + 0.5D + (double) this.randZ * (2.0D * rand.nextDouble() - 1.0D) + (double) this.offsetZ, (double) this.floatArg1 + (double) this.floatRand1 * (2.0D * rand.nextDouble() - 1.0D), (double) this.floatArg2 + (double) this.floatRand2 * (2.0D * rand.nextDouble() - 1.0D), (double) this.floatArg3 + (double) this.floatRand3 * (2.0D * rand.nextDouble() - 1.0D));
                    this.ticksBeforeParticle = this.ticksBetweenParticles;
                }
            }
        }
    }
}
