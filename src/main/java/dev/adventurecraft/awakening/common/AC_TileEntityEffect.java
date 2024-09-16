package dev.adventurecraft.awakening.common;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;

public class AC_TileEntityEffect extends TileEntity {

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

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.particleType = tag.getString("particleType");
        this.particlesPerSpawn = tag.getInt("particlesPerSpawn");
        this.ticksBetweenParticles = tag.getInt("ticksBetweenParticles");
        this.isActivated = tag.getBoolean("isActivated");
        this.offsetX = tag.getFloat("offsetX");
        this.offsetY = tag.getFloat("offsetY");
        this.offsetZ = tag.getFloat("offsetZ");
        this.randX = tag.getFloat("randX");
        this.randY = tag.getFloat("randY");
        this.randZ = tag.getFloat("randZ");
        this.floatArg1 = tag.getFloat("floatArg1");
        this.floatArg2 = tag.getFloat("floatArg2");
        this.floatArg3 = tag.getFloat("floatArg3");
        this.floatRand1 = tag.getFloat("floatRand1");
        this.floatRand2 = tag.getFloat("floatRand2");
        this.floatRand3 = tag.getFloat("floatRand3");
        this.changeFogColor = tag.getInt("changeFogColor");
        this.fogR = tag.getFloat("fogR");
        this.fogG = tag.getFloat("fogG");
        this.fogB = tag.getFloat("fogB");
        this.changeFogDensity = tag.getInt("changeFogDensity");
        this.fogStart = tag.getFloat("fogStart");
        this.fogEnd = tag.getFloat("fogEnd");
        this.setOverlay = tag.getBoolean("setOverlay");
        this.overlay = tag.getString("overlay");
        this.revertTextures = tag.getBoolean("revertTextures");
        this.replaceTextures = tag.getBoolean("replaceTextures");
        this.textureReplacement = tag.getString("textureReplacement");
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);

        if (!this.particleType.equals("")) {
            tag.putString("particleType", this.particleType);
        }

        tag.putInt("particlesPerSpawn", this.particlesPerSpawn);
        tag.putInt("ticksBetweenParticles", this.ticksBetweenParticles);
        tag.putBoolean("isActivated", this.isActivated);
        tag.putFloat("offsetX", this.offsetX);
        tag.putFloat("offsetY", this.offsetY);
        tag.putFloat("offsetZ", this.offsetZ);
        tag.putFloat("randX", this.randX);
        tag.putFloat("randY", this.randY);
        tag.putFloat("randZ", this.randZ);
        tag.putFloat("floatArg1", this.floatArg1);
        tag.putFloat("floatArg2", this.floatArg2);
        tag.putFloat("floatArg3", this.floatArg3);
        tag.putFloat("floatRand1", this.floatRand1);
        tag.putFloat("floatRand2", this.floatRand2);
        tag.putFloat("floatRand3", this.floatRand3);
        tag.putInt("changeFogColor", this.changeFogColor);
        tag.putFloat("fogR", this.fogR);
        tag.putFloat("fogG", this.fogG);
        tag.putFloat("fogB", this.fogB);
        tag.putInt("changeFogDensity", this.changeFogDensity);
        tag.putFloat("fogStart", this.fogStart);
        tag.putFloat("fogEnd", this.fogEnd);
        tag.putBoolean("setOverlay", this.setOverlay);
        tag.putString("overlay", this.overlay);
        tag.putBoolean("revertTextures", this.revertTextures);
        tag.putBoolean("replaceTextures", this.replaceTextures);
        tag.putString("textureReplacement", this.textureReplacement);
    }

    @Override
    public void tick() {
        if (this.checkTrigger) {
            this.isActivated = ((ExWorld) this.level).getTriggerManager().isActivated(this.x, this.y, this.z);
            this.checkTrigger = false;
        }

        if (!this.isActivated) {
            return;
        }

        if (this.ticksBeforeParticle > 0) {
            --this.ticksBeforeParticle;
        } else {
            float pX = this.x + 0.5F + this.offsetX;
            float pY = this.y + 0.5F + this.offsetY;
            float pZ = this.z + 0.5F + this.offsetZ;

            for (int i = 0; i < this.particlesPerSpawn; ++i) {
                this.level.addParticle(
                    this.particleType,
                    pX + this.randX * (2.0F * rand.nextFloat() - 1.0F),
                    pY + this.randY * (2.0F * rand.nextFloat() - 1.0F),
                    pZ + this.randZ * (2.0F * rand.nextFloat() - 1.0F),
                    this.floatArg1 + this.floatRand1 * (2.0F * rand.nextFloat() - 1.0F),
                    this.floatArg2 + this.floatRand2 * (2.0F * rand.nextFloat() - 1.0F),
                    this.floatArg3 + this.floatRand3 * (2.0F * rand.nextFloat() - 1.0F));
            }
            this.ticksBeforeParticle = this.ticksBetweenParticles;
        }
    }
}
