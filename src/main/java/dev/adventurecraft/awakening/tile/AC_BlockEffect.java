package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.common.gui.AC_GuiEffect;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.block.ExFoliageColor;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ptexture.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

import java.io.*;

public class AC_BlockEffect extends TileEntityTile implements AC_ITriggerDebugBlock {

    static boolean needsReloadForRevert = true;

    protected AC_BlockEffect(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityEffect();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    public static boolean replaceTexture(Level world, String keyName, String replacementName) {
        String key = keyName.toLowerCase();
        var texManager = (ExTextureManager) Minecraft.instance.textures;
        if (!((ExWorldProperties) world.levelData).addReplacementTexture(keyName, replacementName)) {
            return false;
        }
        switch (key) {
            case "/watermap.png" -> {
                AC_TerrainImage.loadWaterMap(new File(((ExWorld) world).getLevelDir(), replacementName));
                needsReloadForRevert = true;
                return true;
            }
            case "/biomemap.png" -> {
                AC_TerrainImage.loadBiomeMap(new File(((ExWorld) world).getLevelDir(), replacementName));
                needsReloadForRevert = true;
                return true;
            }
            case "/misc/grasscolor.png" -> {
                ExGrassColor.loadGrass(replacementName, world);
                needsReloadForRevert = true;
                return true;
            }
            case "/misc/foliagecolor.png" -> {
                ExFoliageColor.loadFoliage(replacementName, world);
                needsReloadForRevert = true;
                return true;
            }
            case "/custom_fire.png" -> {
                AC_TextureBinder.loadImages(texManager, FireTexture.class, replacementName, world);
                return true;
            }
            case "/custom_lava_flowing.png" -> {
                AC_TextureBinder.loadImages(texManager, LavaSideTexture.class, replacementName, world);
                return true;
            }
            case "/custom_lava_still.png" -> {
                AC_TextureBinder.loadImages(texManager, LavaTexture.class, replacementName, world);
                return true;
            }
            case "/custom_portal.png" -> {
                AC_TextureBinder.loadImages(texManager, PortalTexture.class, replacementName, world);
                return true;
            }
            case "/custom_water_flowing.png" -> {
                AC_TextureBinder.loadImages(texManager, WaterSideTexture.class, replacementName, world);
                return true;
            }
            case "/custom_water_still.png" -> {
                AC_TextureBinder.loadImages(texManager, WaterTexture.class, replacementName, world);
                return true;
            }
            default -> {
                texManager.replaceTexture(keyName, replacementName);
                return false;
            }
        }
    }

    public static void revertTextures(Level world) {
        ((ExTextureManager) Minecraft.instance.textures).revertTextures();
        if (needsReloadForRevert) {
            ExGrassColor.loadGrass("/misc/grasscolor.png", world);
            ExFoliageColor.loadFoliage("/misc/foliagecolor.png", world);
            var waterFile = new File(((ExWorld) world).getLevelDir(), "waterMap.png");
            if (waterFile.exists()) {
                AC_TerrainImage.loadWaterMap(waterFile);
            }
            var biomeFile = new File(((ExWorld) world).getLevelDir(), "biomeMap.png");
            if (biomeFile.exists()) {
                AC_TerrainImage.loadBiomeMap(biomeFile);
            }
            Minecraft.instance.levelRenderer.skyColorChanged();
            needsReloadForRevert = false;
        }

        ((ExWorldProperties) world.levelData).revertTextures();
    }

    public void replaceTextures(Level keyName, String replacementName) {
        File texFile = new File(((ExWorld) keyName).getLevelDir(), "textureReplacement/" + replacementName);
        if (!texFile.exists()) {
            return;
        }

        boolean replaced = false;

        try {
            var reader = new BufferedReader(new FileReader(texFile));
            try {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] items = line.split(",", 2);
                    if (items.length == 2) {
                        replaced |= replaceTexture(keyName, items[0], items[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (replaced) {
            Minecraft.instance.levelRenderer.skyColorChanged();
        }
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {

        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityEffect entityEffect)) {

            return;
        }
        ExWorldProperties worldProps = (ExWorldProperties) world.levelData;
        entityEffect.isActivated = true;
        entityEffect.ticksBeforeParticle = 0;
        if (entityEffect.changeFogColor == 1) {
            worldProps.setOverrideFogColor(true);
            worldProps.setFogR(entityEffect.fogR);
            worldProps.setFogG(entityEffect.fogG);
            worldProps.setFogB(entityEffect.fogB);
        }
        else if (entityEffect.changeFogColor == 2) {
            worldProps.setOverrideFogColor(false);
        }

        if (entityEffect.changeFogDensity == 1) {
            worldProps.setOverrideFogDensity(true);
            worldProps.setFogStart(entityEffect.fogStart);
            worldProps.setFogEnd(entityEffect.fogEnd);
        }
        else if (entityEffect.changeFogDensity == 2) {
            worldProps.setOverrideFogDensity(false);
        }

        if (entityEffect.setOverlay) {
            worldProps.setOverlay(entityEffect.overlay);
        }

        if (entityEffect.replaceTextures) {
            this.replaceTextures(world, entityEffect.textureReplacement);
        }
        else if (entityEffect.revertTextures) {
            revertTextures(world);
        }
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof AC_TileEntityEffect entityEffect) {
            entityEffect.isActivated = false;
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityEffect entityEffect) {
                AC_GuiEffect.showUI(entityEffect);
                return true;
            }
        }

        return false;
    }
}
