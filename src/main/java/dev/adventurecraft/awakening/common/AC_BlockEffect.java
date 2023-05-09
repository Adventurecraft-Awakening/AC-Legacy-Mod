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

public class AC_BlockEffect extends BlockWithEntity implements AC_ITriggerBlock {

    static boolean needsReloadForRevert = true;

    protected AC_BlockEffect(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityEffect();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        ExWorldProperties worldProps = (ExWorldProperties) world.properties;

        var entity = (AC_TileEntityEffect) world.getBlockEntity(x, y, z);
        entity.isActivated = true;
        entity.ticksBeforeParticle = 0;
        if (entity.changeFogColor == 1) {
            worldProps.setOverrideFogColor(true);
            worldProps.setFogR(entity.fogR);
            worldProps.setFogG(entity.fogG);
            worldProps.setFogB(entity.fogB);
        } else if (entity.changeFogColor == 2) {
            worldProps.setOverrideFogColor(false);
        }

        if (entity.changeFogDensity == 1) {
            worldProps.setOverrideFogDensity(true);
            worldProps.setFogStart(entity.fogStart);
            worldProps.setFogEnd(entity.fogEnd);
        } else if (entity.changeFogDensity == 2) {
            worldProps.setOverrideFogDensity(false);
        }

        if (entity.setOverlay) {
            worldProps.setOverlay(entity.overlay);
        }

        if (entity.replaceTextures) {
            this.replaceTextures(world, entity.textureReplacement);
        } else if (entity.revertTextures) {
            revertTextures(world);
        }
    }

    public static void revertTextures(World world) {
        ((ExTextureManager) Minecraft.instance.textureManager).revertTextures();
        if (needsReloadForRevert) {
            ExGrassColor.loadGrass("/misc/grasscolor.png");
            ExFoliageColor.loadFoliage("/misc/foliagecolor.png");
            AC_TerrainImage.loadWaterMap(new File(((ExWorld) world).getLevelDir(), "watermap.png"));
            AC_TerrainImage.loadBiomeMap(new File(((ExWorld) world).getLevelDir(), "biomemap.png"));
            Minecraft.instance.worldRenderer.method_1148();
            needsReloadForRevert = false;
        }

        ((ExWorldProperties) world.properties).revertTextures();
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

    public static boolean replaceTexture(World world, String var1, String var2) {
        String var3 = var1.toLowerCase();
        ExTextureManager texManager = (ExTextureManager) Minecraft.instance.textureManager;
        if (!((ExWorldProperties) world.properties).addReplacementTexture(var1, var2)) {
            return false;
        } else if (var3.equals("/watermap.png")) {
            AC_TerrainImage.loadWaterMap(new File(((ExWorld) world).getLevelDir(), var2));
            needsReloadForRevert = true;
            return true;
        } else if (var3.equals("/biomemap.png")) {
            AC_TerrainImage.loadBiomeMap(new File(((ExWorld) world).getLevelDir(), var2));
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

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityEffect) world.getBlockEntity(x, y, z);
        entity.isActivated = false;
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            var entity = (AC_TileEntityEffect) world.getBlockEntity(x, y, z);
            AC_GuiEffect.showUI(entity);
            return true;
        } else {
            return false;
        }
    }
}
