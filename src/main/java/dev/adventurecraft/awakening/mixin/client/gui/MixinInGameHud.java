package dev.adventurecraft.awakening.mixin.client.gui;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.common.AC_Version;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.InGameHud;
import net.minecraft.client.gui.ingame.ChatMessage;
import net.minecraft.client.gui.screen.ingame.ChatScreen;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;
import java.util.Random;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud extends GuiElement {

    @Shadow
    private List<ChatMessage> chatMessages;
    @Shadow
    private Random rand;
    @Shadow
    private Minecraft client;
    @Shadow
    private int ticksRan;
    @Shadow
    private String jukeboxMessage;
    @Shadow
    private int jukeboxMessageTime;
    @Shadow
    private boolean isRecordPlaying;

    @Shadow
    protected abstract void renderPumpkinOverlay(int i, int j);

    @Shadow
    protected abstract void renderPortalOverlay(float f, int i, int j);

    @Shadow
    protected abstract void renderHotBarSlot(int i, int j, int k, float f);

    @Shadow
    protected abstract void renderVingette(float f, int i, int j);

    //public ScriptUIContainer scriptUI; TODO
    public boolean hudEnabled = true;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft var1, CallbackInfo ci) {
        //this.scriptUI = new ScriptUIContainer(0.0F, 0.0F, (ScriptUIContainer)null); TODO
    }

    @Overwrite
    public void render(float var1, boolean var2, int var3, int var4) {
        ScreenScaler var5 = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
        int var6 = var5.getScaledWidth();
        int var7 = var5.getScaledHeight();
        TextRenderer textRenderer = this.client.textRenderer;
        this.client.gameRenderer.method_1843();
        GL11.glEnable(GL11.GL_BLEND);
        if (Minecraft.isFancyGraphicsEnabled()) {
            this.renderVingette(this.client.player.getBrightnessAtEyes(var1), var6, var7);
        }

        if (!this.client.options.thirdPerson && !((ExMinecraft) this.client).isCameraActive()) {
            ItemStack headItem = this.client.player.inventory.getArmorItem(3);
            if (headItem != null && headItem.itemId == Block.PUMPKIN.id) {
                this.renderPumpkinOverlay(var6, var7);
            }
        }

        if (this.client.world != null) {
            String overlay = ((ExWorldProperties) this.client.world.properties).getOverlay();
            if (!overlay.isEmpty()) {
                this.renderOverlay(var6, var7, overlay);
            }
        }

        float var10 = this.client.player.field_505 + (this.client.player.field_504 - this.client.player.field_505) * var1;
        if (var10 > 0.0F) {
            this.renderPortalOverlay(var10, var6, var7);
        }

        boolean var12;
        int var16;
        int var18;
        if (this.hudEnabled) {
            int maxHealth = ((ExLivingEntity) this.client.player).getMaxHealth();

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/gui.png"));
            PlayerInventory var11 = this.client.player.inventory;
            this.zOffset = -90.0F;
            this.blit(var6 / 2 - 91, var7 - 22, 0, 0, 182, 22);
            this.blit(var6 / 2 - 91 - 1 + ((ExPlayerInventory) var11).getOffhandItem() * 20, var7 - 22 - 1, 24, 22, 48, 22);
            this.blit(var6 / 2 - 91 - 1 + var11.selectedHotBarSlot * 20, var7 - 22 - 1, 0, 22, 24, 22);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/icons.png"));
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
            this.blit(var6 / 2 - 7, var7 / 2 - 7, 0, 0, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
            var12 = this.client.player.field_1613 / 3 % 2 == 1;
            if (this.client.player.field_1613 < 10) {
                var12 = false;
            }

            int var13 = this.client.player.health;
            int var14 = this.client.player.prevHealth;
            this.rand.setSeed(this.ticksRan * 312871);
            int var15;
            int var17;
            if (this.client.interactionManager.method_1722()) {
                var15 = this.client.player.getArmorValue();

                for (var16 = 0; var16 < 10; ++var16) {
                    var17 = var7 - 32;
                    if (var15 > 0) {
                        var18 = var6 / 2 + 91 - var16 * 8 - 9;
                        if (var16 * 2 + 1 < var15) {
                            this.blit(var18, var17, 34, 9, 9, 9);
                        }

                        if (var16 * 2 + 1 == var15) {
                            this.blit(var18, var17, 25, 9, 9, 9);
                        }

                        if (var16 * 2 + 1 > var15) {
                            this.blit(var18, var17, 16, 9, 9, 9);
                        }
                    }

                    var18 = var6 / 2 - 91 + var16 * 8;
                    if (var13 <= 8) {
                        var17 += this.rand.nextInt(2);
                    }

                    for (int var19 = 0; var19 <= (maxHealth - 1) / 40; ++var19) {
                        if ((var16 + 1 + var19 * 10) * 4 <= maxHealth) {
                            byte var20 = 0;
                            if (var12) {
                                var20 = 1;
                            }

                            this.blit(var18, var17, 16 + var20 * 9, 0, 9, 9);
                            if (var12) {
                                if (var16 * 4 + 3 + var19 * 40 < var14) {
                                    this.blit(var18, var17, 70, 0, 9, 9);
                                } else if (var16 * 4 + 3 + var19 * 40 == var14) {
                                    this.blit(var18, var17, 105, 0, 9, 9);
                                } else if (var16 * 4 + 2 + var19 * 40 == var14) {
                                    this.blit(var18, var17, 79, 0, 9, 9);
                                } else if (var16 * 4 + 1 + var19 * 40 == var14) {
                                    this.blit(var18, var17, 114, 0, 9, 9);
                                }
                            }

                            if (var16 * 4 + 3 + var19 * 40 < var13) {
                                this.blit(var18, var17, 52, 0, 9, 9);
                            } else if (var16 * 4 + 3 + var19 * 40 == var13) {
                                this.blit(var18, var17, 87, 0, 9, 9);
                            } else if (var16 * 4 + 2 + var19 * 40 == var13) {
                                this.blit(var18, var17, 61, 0, 9, 9);
                            } else if (var16 * 4 + 1 + var19 * 40 == var13) {
                                this.blit(var18, var17, 96, 0, 9, 9);
                            }
                        }

                        var17 -= 9;
                    }
                }
            }

            if (this.client.player.isInFluid(Material.WATER)) {
                var15 = -9 * ((maxHealth - 1) / 40);
                var16 = (int) Math.ceil((double) (this.client.player.air - 2) * 10.0D / 300.0D);
                var17 = (int) Math.ceil((double) this.client.player.air * 10.0D / 300.0D) - var16;

                for (var18 = 0; var18 < var16 + var17; ++var18) {
                    if (var18 < var16) {
                        this.blit(var6 / 2 - 91 + var18 * 8, var7 - 32 - 9 + var15, 16, 18, 9, 9);
                    } else {
                        this.blit(var6 / 2 - 91 + var18 * 8, var7 - 32 - 9 + var15, 25, 18, 9, 9);
                    }
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        int var28;
        int var30;
        if (this.hudEnabled) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            RenderHelper.enableLighting();
            GL11.glPopMatrix();

            for (var28 = 0; var28 < 9; ++var28) {
                var30 = var6 / 2 - 90 + var28 * 20 + 2;
                int var13 = var7 - 16 - 3;
                this.renderHotBarSlot(var28, var30, var13, var1);
            }

            RenderHelper.disableLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        if (this.client.player.getSleepTimer() > 0) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            var28 = this.client.player.getSleepTimer();
            float var31 = (float) var28 / 100.0F;
            if (var31 > 1.0F) {
                var31 = 1.0F - (float) (var28 - 100) / 10.0F;
            }

            int var13 = (int) (220.0F * var31) << 24 | 1052704;
            this.fill(0, 0, var6, var7, var13);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        String var39;
        if (this.client.options.debugHud) {
            GL11.glPushMatrix();
            if (Minecraft.isPremiumCheckTime > 0L) {
                GL11.glTranslatef(0.0F, 32.0F, 0.0F);
            }

            textRenderer.drawTextWithShadow("Minecraft Beta 1.7.3 (" + this.client.fpsDebugString + ")", 2, 2, 16777215);
            textRenderer.drawTextWithShadow(this.client.getDebugFirstLine(), 2, 12, 16777215);
            textRenderer.drawTextWithShadow(this.client.getDebugSecondLine(), 2, 22, 16777215);
            textRenderer.drawTextWithShadow(this.client.getDebugThirdLine(), 2, 32, 16777215);
            textRenderer.drawTextWithShadow(this.client.getDebugFourthLine(), 2, 42, 16777215);
            long var29 = Runtime.getRuntime().maxMemory();
            long var33 = Runtime.getRuntime().totalMemory();
            long var36 = Runtime.getRuntime().freeMemory();
            long var37 = var33 - var36;
            var39 = "Used memory: " + var37 * 100L / var29 + "% (" + var37 / 1024L / 1024L + "MB) of " + var29 / 1024L / 1024L + "MB";
            this.drawTextWithShadow(textRenderer, var39, var6 - textRenderer.getTextWidth(var39) - 2, 2, 14737632);
            var39 = "Allocated memory: " + var33 * 100L / var29 + "% (" + var33 / 1024L / 1024L + "MB)";
            this.drawTextWithShadow(textRenderer, var39, var6 - textRenderer.getTextWidth(var39) - 2, 12, 14737632);
            this.drawTextWithShadow(textRenderer, "x: " + this.client.player.x, 2, 64, 14737632);
            this.drawTextWithShadow(textRenderer, "y: " + this.client.player.y, 2, 72, 14737632);
            this.drawTextWithShadow(textRenderer, "z: " + this.client.player.z, 2, 80, 14737632);
            this.drawTextWithShadow(textRenderer, "f: " + (MathHelper.floor((double) (this.client.player.yaw * 4.0F / 360.0F) + 0.5D) & 3), 2, 88, 14737632);
            boolean useWorldGenImages = ((ExWorldProperties) this.client.world.properties).getWorldGenProps().useImages;
            this.drawTextWithShadow(textRenderer, String.format("Use Terrain Images: %b", useWorldGenImages), 2, 96, 14737632);
            var exPlayer = (ExEntity) this.client.player;
            this.drawTextWithShadow(textRenderer, String.format("Collide X: %d Z: %d", exPlayer.getCollisionX(), exPlayer.getCollisionZ()), 2, 104, 14737632);
            if (useWorldGenImages) {
                int var40 = (int) this.client.player.x;
                int var21 = (int) this.client.player.z;
                int var22 = AC_TerrainImage.getTerrainHeight(var40, var21);
                int var23 = AC_TerrainImage.getWaterHeight(var40, var21);
                double var24 = AC_TerrainImage.getTerrainTemperature(var40, var21);
                double var26 = AC_TerrainImage.getTerrainHumidity(var40, var21);
                this.drawTextWithShadow(textRenderer, String.format("T: %d W: %d Temp: %.2f Humid: %.2f", var22, var23, var24, var26), 2, 112, 14737632);
            }

            GL11.glPopMatrix();
        } else {
            textRenderer.drawTextWithShadow(AC_Version.shortVersion, 2, 2, 16777215);
            var28 = 12;
            if (AC_DebugMode.active) {
                textRenderer.drawTextWithShadow("Debug Active", 2, var28, 16777215);
                var28 += 10;
            }

            if (AC_DebugMode.levelEditing) {
                textRenderer.drawTextWithShadow("Map Editing", 2, var28, 16777215);
            }
        }

        if (this.jukeboxMessageTime > 0) {
            float var32 = (float) this.jukeboxMessageTime - var1;
            var30 = (int) (var32 * 256.0F / 20.0F);
            if (var30 > 255) {
                var30 = 255;
            }

            if (var30 > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) (var6 / 2), (float) (var7 - 48), 0.0F);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                int var13 = 16777215;
                if (this.isRecordPlaying) {
                    var13 = Color.HSBtoRGB(var32 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                textRenderer.drawText(this.jukeboxMessage, -textRenderer.getTextWidth(this.jukeboxMessage) / 2, -4, var13 + (var30 << 24));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        //this.scriptUI.render(textRenderer, this.client.textureManager, var1); TODO
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        byte var34 = 10;
        var12 = false;
        if (this.client.currentScreen instanceof ChatScreen) {
            var34 = 20;
            var12 = true;
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, (float) (var7 - 48), 0.0F);

        for (int i = 0; i < this.chatMessages.size() && i < var34; ++i) {
            ChatMessage message = this.chatMessages.get(i);
            if (message.messageAge < 200 || var12) {
                double var35 = (double) message.messageAge / 200.0D;
                var35 = 1.0D - var35;
                var35 *= 10.0D;
                if (var35 < 0.0D) {
                    var35 = 0.0D;
                }

                if (var35 > 1.0D) {
                    var35 = 1.0D;
                }

                var35 *= var35;
                var16 = (int) (255.0D * var35);
                if (var12) {
                    var16 = 255;
                }

                if (var16 > 0) {
                    byte var38 = 2;
                    var18 = -i * 9;
                    var39 = message.messageText;
                    this.fill(var38, var18 - 1, var38 + 320, var18 + 8, var16 / 2 << 24);
                    GL11.glEnable(GL11.GL_BLEND);
                    textRenderer.drawTextWithShadow(var39, var38, var18, 16777215 + (var16 << 24));
                }
            }
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderOverlay(int var1, int var2, String var3) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/overlays/" + var3));
        Tessellator var4 = Tessellator.INSTANCE;
        var4.start();
        var4.vertex(0.0D, var2, -90.0D, 0.0D, 1.0D);
        var4.vertex(var1, var2, -90.0D, 1.0D, 1.0D);
        var4.vertex(var1, 0.0D, -90.0D, 1.0D, 0.0D);
        var4.vertex(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        var4.tessellate();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
