package dev.adventurecraft.awakening.tile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiEffect;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.block.ExFoliageColor;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
//import net.minecraft.client.render.*;
import net.minecraft.client.renderer.ptexture.FireTexture;
import net.minecraft.client.renderer.ptexture.LavaSideTexture;
import net.minecraft.client.renderer.ptexture.LavaTexture;
import net.minecraft.client.renderer.ptexture.PortalTexture;
import net.minecraft.client.renderer.ptexture.WaterSideTexture;
import net.minecraft.client.renderer.ptexture.WaterTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockEffect extends TileEntityTile implements AC_ITriggerBlock {

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
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        ExWorldProperties worldProps = (ExWorldProperties) world.levelData;

        var entity = (AC_TileEntityEffect) world.getTileEntity(x, y, z);
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

    public static void revertTextures(Level world) {
        ((ExTextureManager) Minecraft.instance.textures).revertTextures();
        if (needsReloadForRevert) {
            ExGrassColor.loadGrass("/misc/grasscolor.png", world);
            ExFoliageColor.loadFoliage("/misc/foliagecolor.png", world);
            AC_TerrainImage.loadWaterMap(new File(((ExWorld) world).getLevelDir(), "watermap.png"));
            AC_TerrainImage.loadBiomeMap(new File(((ExWorld) world).getLevelDir(), "biomemap.png"));
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

    public static boolean replaceTexture(Level world, String keyName, String replacementName) {
        String key = keyName.toLowerCase();
        var texManager = (ExTextureManager) Minecraft.instance.textures;
        if (!((ExWorldProperties) world.levelData).addReplacementTexture(keyName, replacementName)) {
            return false;
        } else if (key.equals("/watermap.png")) {
            AC_TerrainImage.loadWaterMap(new File(((ExWorld) world).getLevelDir(), replacementName));
            needsReloadForRevert = true;
            return true;
        } else if (key.equals("/biomemap.png")) {
            AC_TerrainImage.loadBiomeMap(new File(((ExWorld) world).getLevelDir(), replacementName));
            needsReloadForRevert = true;
            return true;
        } else if (key.equals("/misc/grasscolor.png")) {
            ExGrassColor.loadGrass(replacementName, world);
            needsReloadForRevert = true;
            return true;
        } else if (key.equals("/misc/foliagecolor.png")) {
            ExFoliageColor.loadFoliage(replacementName, world);
            needsReloadForRevert = true;
            return true;
        } else if (key.equals("/custom_fire.png")) {
            AC_TextureBinder.loadImages(texManager, FireTexture.class, replacementName, world);
            return true;
        } else if (key.equals("/custom_lava_flowing.png")) {
            AC_TextureBinder.loadImages(texManager, LavaSideTexture.class, replacementName, world);
            return true;
        } else if (key.equals("/custom_lava_still.png")) {
            AC_TextureBinder.loadImages(texManager, LavaTexture.class, replacementName, world);
            return true;
        } else if (key.equals("/custom_portal.png")) {
            AC_TextureBinder.loadImages(texManager, PortalTexture.class, replacementName, world);
            return true;
        } else if (key.equals("/custom_water_flowing.png")) {
            AC_TextureBinder.loadImages(texManager, WaterSideTexture.class, replacementName, world);
            return true;
        } else if (key.equals("/custom_water_still.png")) {
            AC_TextureBinder.loadImages(texManager, WaterTexture.class, replacementName, world);
            return true;
        } else {
            texManager.replaceTexture(keyName, replacementName);
            return false;
        }
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityEffect) world.getTileEntity(x, y, z);
        entity.isActivated = false;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active && (player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            var entity = (AC_TileEntityEffect) world.getTileEntity(x, y, z);
            AC_GuiEffect.showUI(entity);
            return true;
        } else {
            return false;
        }
    }
}
