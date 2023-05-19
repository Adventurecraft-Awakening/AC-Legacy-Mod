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

    @Override
    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);

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
    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);

        if (!this.particleType.equals("")) {
            tag.put("particleType", this.particleType);
        }

        tag.put("particlesPerSpawn", this.particlesPerSpawn);
        tag.put("ticksBetweenParticles", this.ticksBetweenParticles);
        tag.put("isActivated", this.isActivated);
        tag.put("offsetX", this.offsetX);
        tag.put("offsetY", this.offsetY);
        tag.put("offsetZ", this.offsetZ);
        tag.put("randX", this.randX);
        tag.put("randY", this.randY);
        tag.put("randZ", this.randZ);
        tag.put("floatArg1", this.floatArg1);
        tag.put("floatArg2", this.floatArg2);
        tag.put("floatArg3", this.floatArg3);
        tag.put("floatRand1", this.floatRand1);
        tag.put("floatRand2", this.floatRand2);
        tag.put("floatRand3", this.floatRand3);
        tag.put("changeFogColor", this.changeFogColor);
        tag.put("fogR", this.fogR);
        tag.put("fogG", this.fogG);
        tag.put("fogB", this.fogB);
        tag.put("changeFogDensity", this.changeFogDensity);
        tag.put("fogStart", this.fogStart);
        tag.put("fogEnd", this.fogEnd);
        tag.put("setOverlay", this.setOverlay);
        tag.put("overlay", this.overlay);
        tag.put("revertTextures", this.revertTextures);
        tag.put("replaceTextures", this.replaceTextures);
        tag.put("textureReplacement", this.textureReplacement);
    }

    @Override
    public void tick() {
        if (this.checkTrigger) {
            this.isActivated = ((ExWorld) this.world).getTriggerManager().isActivated(this.x, this.y, this.z);
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
                this.world.addParticle(
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
