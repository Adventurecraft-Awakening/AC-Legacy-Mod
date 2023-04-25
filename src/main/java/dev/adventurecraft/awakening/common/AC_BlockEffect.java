package dev.adventurecraft.awakening.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.block.ExFoliageColor;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.*;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockEffect extends BlockWithEntity {
    static boolean needsReloadForRevert = true;

    protected AC_BlockEffect(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityEffect();
    }

    public boolean isFullOpaque() {
        return false;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        ExWorldProperties worldProps = (ExWorldProperties) var1.properties;

        AC_TileEntityEffect var5 = (AC_TileEntityEffect) var1.getBlockEntity(var2, var3, var4);
        var5.isActivated = true;
        var5.ticksBeforeParticle = 0;
        if (var5.changeFogColor == 1) {
            worldProps.setOverrideFogColor(true);
            worldProps.setFogR(var5.fogR);
            worldProps.setFogG(var5.fogG);
            worldProps.setFogB(var5.fogB);
        } else if (var5.changeFogColor == 2) {
            worldProps.setOverrideFogColor(false);
        }

        if (var5.changeFogDensity == 1) {
            worldProps.setOverrideFogDensity(true);
            worldProps.setFogStart(var5.fogStart);
            worldProps.setFogEnd(var5.fogEnd);
        } else if (var5.changeFogDensity == 2) {
            worldProps.setOverrideFogDensity(false);
        }

        if (var5.setOverlay) {
            worldProps.setOverlay(var5.overlay);
        }

        if (var5.replaceTextures) {
            this.replaceTextures(var1, var5.textureReplacement);
        } else if (var5.revertTextures) {
            revertTextures(var1);
        }
    }

    public static void revertTextures(World var0) {
        ((ExTextureManager) Minecraft.instance.textureManager).revertTextures();
        if (needsReloadForRevert) {
            ExGrassColor.loadGrass("/misc/grasscolor.png");
            ExFoliageColor.loadFoliage("/misc/foliagecolor.png");
            AC_TerrainImage.loadWaterMap(new File(((ExWorld) var0).getLevelDir(), "watermap.png"));
            AC_TerrainImage.loadBiomeMap(new File(((ExWorld) var0).getLevelDir(), "biomemap.png"));
            Minecraft.instance.worldRenderer.method_1148();
            needsReloadForRevert = false;
        }

        ((ExWorldProperties) var0.properties).revertTextures();
    }

    public void replaceTextures(World var1, String var2) {
        boolean var3 = false;
        File var4 = new File(((ExWorld) var1).getLevelDir(), "textureReplacement/" + var2);
        if (var4.exists()) {
            try {
                BufferedReader var5 = new BufferedReader(new FileReader(var4));

                try {
                    while (var5.ready()) {
                        String var6 = var5.readLine();
                        String[] var7 = var6.split(",", 2);
                        if (var7.length == 2) {
                            var3 |= replaceTexture(var1, var7[0], var7[1]);
                        }
                    }
                } catch (IOException var8) {
                    var8.printStackTrace();
                }
            } catch (FileNotFoundException var9) {
                var9.printStackTrace();
            }
        }

        if (var3) {
            Minecraft.instance.worldRenderer.method_1148();
        }
    }

    public static boolean replaceTexture(World var0, String var1, String var2) {
        String var3 = var1.toLowerCase();
        ExTextureManager texManager = (ExTextureManager) Minecraft.instance.textureManager;
        if (!((ExWorldProperties) var0.properties).addReplacementTexture(var1, var2)) {
            return false;
        } else if (var3.equals("/watermap.png")) {
            AC_TerrainImage.loadWaterMap(new File(((ExWorld) var0).getLevelDir(), var2));
            needsReloadForRevert = true;
            return true;
        } else if (var3.equals("/biomemap.png")) {
            AC_TerrainImage.loadBiomeMap(new File(((ExWorld) var0).getLevelDir(), var2));
            needsReloadForRevert = true;
            return true;
        } else if (var3.equals("/misc/grasscolor.png")) {
            ExGrassColor.loadGrass(var2);
            needsReloadForRevert = true;
            return true;
        } else if (var3.equals("/misc/foliagecolor.png")) {
            ExFoliageColor.loadFoliage(var2);
            needsReloadForRevert = true;
            return true;
        } else if (var3.equals("/custom_fire.png")) {
            ((AC_TextureBinder) texManager.getTextureBinder(FireTextureBinder.class)).loadImage(var2);
            return true;
        } else if (var3.equals("/custom_lava_flowing.png")) {
            ((AC_TextureBinder) texManager.getTextureBinder(FlowingLavaTextureBinder2.class)).loadImage(var2);
            return true;
        } else if (var3.equals("/custom_lava_still.png")) {
            ((AC_TextureBinder) texManager.getTextureBinder(FlowingLavaTextureBinder.class)).loadImage(var2);
            return true;
        } else if (var3.equals("/custom_portal.png")) {
            ((AC_TextureBinder) texManager.getTextureBinder(PortalTextureBinder.class)).loadImage(var2);
            return true;
        } else if (var3.equals("/custom_water_flowing.png")) {
            ((AC_TextureBinder) texManager.getTextureBinder(FlowingWaterTextureBinder.class)).loadImage(var2);
            return true;
        } else if (var3.equals("/custom_water_still.png")) {
            ((AC_TextureBinder) texManager.getTextureBinder(FlowingWaterTextureBinder2.class)).loadImage(var2);
            return true;
        } else {
            texManager.replaceTexture(var1, var2);
            return false;
        }
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityEffect var5 = (AC_TileEntityEffect) var1.getBlockEntity(var2, var3, var4);
        var5.isActivated = false;
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityEffect var6 = (AC_TileEntityEffect) var1.getBlockEntity(var2, var3, var4);
            AC_GuiEffect.showUI(var6);
            return true;
        } else {
            return false;
        }
    }
}
