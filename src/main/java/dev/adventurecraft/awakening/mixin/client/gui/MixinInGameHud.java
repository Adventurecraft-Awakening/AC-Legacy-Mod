package dev.adventurecraft.awakening.mixin.client.gui;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.util.HexConvert;
import dev.adventurecraft.awakening.script.ScriptUIContainer;
import net.minecraft.client.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Random;

@Mixin(Gui.class)
public abstract class MixinInGameHud extends GuiComponent implements ExInGameHud {

    private static final String[] CODE_TO_ANSI_SEQUENCE = new String[]{
        // Regular
        "\033[0;30m", // BLACK
        "\033[0;34m", // BLUE
        "\033[0;32m", // GREEN
        "\033[0;36m", // CYAN
        "\033[0;31m", // RED
        "\033[0;35m", // PURPLE
        "\033[0;33m", // YELLOW
        "\033[0;37m", // WHITE

        // High Intensity
        "\033[0;90m", // BLACK
        "\033[0;94m", // BLUE
        "\033[0;92m", // GREEN
        "\033[0;96m", // CYAN
        "\033[0;91m", // RED
        "\033[0;95m", // PURPLE
        "\033[0;93m", // YELLOW
        "\033[0;97m", // WHITE
    };

    private static final int CHAT_WIDTH = 320;

    @Shadow
    private Random random;
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private int tickCount;
    @Shadow
    private String nowPlayingString;
    @Shadow
    private int nowPlayingTime;
    @Shadow
    private boolean animateOverlayMessageColor;

    @Shadow
    protected abstract void renderPumpkin(int i, int j);

    @Shadow
    protected abstract void renderPortalOverlay(float f, int i, int j);

    @Shadow
    protected abstract void renderSlot(int i, int j, int k, float f);

    @Shadow
    protected abstract void renderVignette(float f, int i, int j);

    private ArrayDeque<AC_ChatMessage> chatMessages;
    public ScriptUIContainer scriptUI;
    public boolean hudEnabled = true;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft var1, CallbackInfo ci) {
        this.chatMessages = new ArrayDeque<>();
        this.scriptUI = new ScriptUIContainer(0.0F, 0.0F, null);
    }

    @Overwrite
    public void render(float var1, boolean var2, int var3, int var4) {
        ScreenSizeCalculator scaler = new ScreenSizeCalculator(this.minecraft.options, this.minecraft.width, this.minecraft.height);
        int screenWidth = scaler.getWidth();
        int screenHeight = scaler.getHeight();
        Font textRenderer = this.minecraft.font;
        this.minecraft.gameRenderer.setScreenProjectionMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(this.minecraft.player.getBrightness(var1), screenWidth, screenHeight);
        }

        if (!this.minecraft.options.thirdPersonView && !((ExMinecraft) this.minecraft).isCameraActive()) {
            ItemInstance headItem = this.minecraft.player.inventory.getArmor(3);
            if (headItem != null && headItem.id == Tile.PUMPKIN.id) {
                this.renderPumpkin(screenWidth, screenHeight);
            }
        }

        if (this.minecraft.level != null) {
            String overlay = ((ExWorldProperties) this.minecraft.level.levelData).getOverlay();
            if (!overlay.isEmpty()) {
                this.renderOverlay(screenWidth, screenHeight, overlay);
            }
        }

        float var10 = this.minecraft.player.oPortalTime + (this.minecraft.player.portalTime - this.minecraft.player.oPortalTime) * var1;
        if (var10 > 0.0F) {
            this.renderPortalOverlay(var10, screenWidth, screenHeight);
        }

        if (this.minecraft.level != null) {
            // Refresh hudEnabled property (has to be here, because ui.hudEnabled can be set directly....)
            ((ExWorldProperties) this.minecraft.level.levelData).setHudEnabled(this.hudEnabled);
        }

        if (this.hudEnabled) {
            int maxHealth = ((ExMob) this.minecraft.player).getMaxHealth();

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textures.loadTexture("/gui/gui.png"));
            Inventory var11 = this.minecraft.player.inventory;
            this.blitOffset = -90.0F;
            this.blit(screenWidth / 2 - 91, screenHeight - 22, 0, 0, 182, 22);
            this.blit(screenWidth / 2 - 91 - 1 + ((ExPlayerInventory) var11).getOffhandItem() * 20, screenHeight - 22 - 1, 24, 22, 48, 22);
            this.blit(screenWidth / 2 - 91 - 1 + var11.selected * 20, screenHeight - 22 - 1, 0, 22, 24, 22);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textures.loadTexture("/gui/icons.png"));
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
            this.blit(screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
            boolean isChatOpen = this.minecraft.player.invulnerableTime / 3 % 2 == 1;
            if (this.minecraft.player.invulnerableTime < 10) {
                isChatOpen = false;
            }

            int playerHealth = this.minecraft.player.health;
            int playerPrevHealth = this.minecraft.player.lastHealth;
            this.random.setSeed(this.tickCount * 312871);

            if (this.minecraft.gameMode.canHurtPlayer()) {
                int playerArmor = this.minecraft.player.getArmor();

                for (int armorIndex = 0; armorIndex < 10; ++armorIndex) {
                    int y = screenHeight - 32;
                    if (playerArmor > 0) {
                        int armorX = screenWidth / 2 + 91 - armorIndex * 8 - 9;
                        if (armorIndex * 2 + 1 < playerArmor) {
                            this.blit(armorX, y, 34, 9, 9, 9);
                        }

                        if (armorIndex * 2 + 1 == playerArmor) {
                            this.blit(armorX, y, 25, 9, 9, 9);
                        }

                        if (armorIndex * 2 + 1 > playerArmor) {
                            this.blit(armorX, y, 16, 9, 9, 9);
                        }
                    }

                    int healthX = screenWidth / 2 - 91 + armorIndex * 8;
                    if (playerHealth <= 8) {
                        y += this.random.nextInt(2);
                    }

                    for (int healthIndex = 0; healthIndex <= (maxHealth - 1) / 40; ++healthIndex) {
                        if ((armorIndex + 1 + healthIndex * 10) * 4 <= maxHealth) {
                            int chatYOffset = 0;
                            if (isChatOpen) {
                                chatYOffset = 1;
                            }

                            this.blit(healthX, y, 16 + chatYOffset * 9, 0, 9, 9);
                            if (isChatOpen) {
                                if (armorIndex * 4 + 3 + healthIndex * 40 < playerPrevHealth) {
                                    this.blit(healthX, y, 70, 0, 9, 9);
                                } else if (armorIndex * 4 + 3 + healthIndex * 40 == playerPrevHealth) {
                                    this.blit(healthX, y, 105, 0, 9, 9);
                                } else if (armorIndex * 4 + 2 + healthIndex * 40 == playerPrevHealth) {
                                    this.blit(healthX, y, 79, 0, 9, 9);
                                } else if (armorIndex * 4 + 1 + healthIndex * 40 == playerPrevHealth) {
                                    this.blit(healthX, y, 114, 0, 9, 9);
                                }
                            }

                            if (armorIndex * 4 + 3 + healthIndex * 40 < playerHealth) {
                                this.blit(healthX, y, 52, 0, 9, 9);
                            } else if (armorIndex * 4 + 3 + healthIndex * 40 == playerHealth) {
                                this.blit(healthX, y, 87, 0, 9, 9);
                            } else if (armorIndex * 4 + 2 + healthIndex * 40 == playerHealth) {
                                this.blit(healthX, y, 61, 0, 9, 9);
                            } else if (armorIndex * 4 + 1 + healthIndex * 40 == playerHealth) {
                                this.blit(healthX, y, 96, 0, 9, 9);
                            }
                        }
                        y -= 9;
                    }
                }
            }

            if (this.minecraft.player.isUnderLiquid(Material.WATER)) {
                int healthYOffset = -9 * ((maxHealth - 1) / 40);
                int alpha = (int) Math.ceil((double) (this.minecraft.player.airSupply - 2) * 10.0D / 300.0D);
                int airUsed = (int) Math.ceil((double) this.minecraft.player.airSupply * 10.0D / 300.0D) - alpha;

                for (int bubbleIndex = 0; bubbleIndex < alpha + airUsed; ++bubbleIndex) {
                    if (bubbleIndex < alpha) {
                        this.blit(screenWidth / 2 - 91 + bubbleIndex * 8, screenHeight - 32 - 9 + healthYOffset, 16, 18, 9, 9);
                    } else {
                        this.blit(screenWidth / 2 - 91 + bubbleIndex * 8, screenHeight - 32 - 9 + healthYOffset, 25, 18, 9, 9);
                    }
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        if (this.hudEnabled) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            Lighting.turnOn();
            GL11.glPopMatrix();

            for (int slot = 0; slot < 9; ++slot) {
                int x = screenWidth / 2 - 90 + slot * 20 + 2;
                int y = screenHeight - 16 - 3;
                this.renderSlot(slot, x, y, var1);
            }

            Lighting.turnOff();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        if (this.minecraft.player.getSleepTimer() > 0) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            int sleepTimer = this.minecraft.player.getSleepTimer();
            float sleepFactor = (float) sleepTimer / 100.0F;
            if (sleepFactor > 1.0F) {
                sleepFactor = 1.0F - (float) (sleepTimer - 100) / 10.0F;
            }

            int color = (int) (220.0F * sleepFactor) << 24 | 1052704;
            this.fill(0, 0, screenWidth, screenHeight, color);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        if (this.minecraft.options.renderDebug) {
            GL11.glPushMatrix();
            if (Minecraft.sessionTime > 0L) {
                GL11.glTranslatef(0.0F, 32.0F, 0.0F);
            }

            textRenderer.drawShadow("Minecraft Beta 1.7.3 (" + this.minecraft.fpsString + ")", 2, 2, 16777215);
            textRenderer.drawShadow(this.minecraft.getChunkStatistics(), 2, 12, 16777215);
            textRenderer.drawShadow(this.minecraft.getEntityStatistics(), 2, 22, 16777215);
            textRenderer.drawShadow(this.minecraft.getParticleStatistics(), 2, 32, 16777215);
            textRenderer.drawShadow(this.minecraft.getDebugInfo(), 2, 42, 16777215);

            long maxMem = Runtime.getRuntime().maxMemory();
            long totMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long usedMem = totMem - freeMem;
            String var39 = "Used memory: " + usedMem * 100L / maxMem + "% (" + usedMem / 1024L / 1024L + "MB) of " + maxMem / 1024L / 1024L + "MB";
            this.drawString(textRenderer, var39, screenWidth - textRenderer.width(var39) - 2, 2, 14737632);
            var39 = "Allocated memory: " + totMem * 100L / maxMem + "% (" + totMem / 1024L / 1024L + "MB)";

            this.drawString(textRenderer, var39, screenWidth - textRenderer.width(var39) - 2, 12, 14737632);
            this.drawString(textRenderer, "x: " + this.minecraft.player.x, 2, 64, 14737632);
            this.drawString(textRenderer, "y: " + this.minecraft.player.y, 2, 72, 14737632);
            this.drawString(textRenderer, "z: " + this.minecraft.player.z, 2, 80, 14737632);
            this.drawString(textRenderer, "f: " + (Mth.floor((double) (this.minecraft.player.yRot * 4.0F / 360.0F) + 0.5D) & 3), 2, 88, 14737632);

            boolean useWorldGenImages = ((ExWorldProperties) this.minecraft.level.levelData).getWorldGenProps().useImages;
            this.drawString(textRenderer, String.format("Use Terrain Images: %b", useWorldGenImages), 2, 96, 14737632);

            var exPlayer = (ExEntity) this.minecraft.player;
            this.drawString(textRenderer, String.format("Collide X: %d Z: %d", exPlayer.getCollisionX(), exPlayer.getCollisionZ()), 2, 104, 14737632);

            if (useWorldGenImages) {
                int var40 = (int) this.minecraft.player.x;
                int var21 = (int) this.minecraft.player.z;
                int var22 = AC_TerrainImage.getTerrainHeight(var40, var21);
                int var23 = AC_TerrainImage.getWaterHeight(var40, var21);
                double var24 = AC_TerrainImage.getTerrainTemperature(var40, var21);
                double var26 = AC_TerrainImage.getTerrainHumidity(var40, var21);
                this.drawString(textRenderer, String.format("T: %d W: %d Temp: %.2f Humid: %.2f", var22, var23, var24, var26), 2, 112, 14737632);
            }

            GL11.glPopMatrix();
        } else {
            int y = 0; // 12 prev
            if (AC_DebugMode.active) {
                textRenderer.drawShadow(AC_Version.shortVersion, 2, 2, 16777215);
                textRenderer.drawShadow("Debug Active", 2, 12, 16777215);
                y += 22;
            }

            if (AC_DebugMode.levelEditing) {
                textRenderer.drawShadow("Map Editing", 2, y, 16777215);
            }
        }

        if (this.nowPlayingTime > 0) {
            float var32 = (float) this.nowPlayingTime - var1;
            int alpha = (int) (var32 * 256.0F / 20.0F);
            if (alpha > 255) {
                alpha = 255;
            }

            if (alpha > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) (screenWidth / 2), (float) (screenHeight - 48), 0.0F);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                int color = 16777215;
                if (this.animateOverlayMessageColor) {
                    color = Color.HSBtoRGB(var32 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                textRenderer.draw(this.nowPlayingString, -textRenderer.width(this.nowPlayingString) / 2, -4, color + (alpha << 24));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        this.scriptUI.render(textRenderer, this.minecraft.textures, var1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        this.renderChat(screenHeight);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    private void renderChat(int screenHeight) {
        var exTextRenderer = (ExTextRenderer) this.minecraft.font;
        ArrayDeque<AC_ChatMessage> messages = this.chatMessages;

        final int marginLeft = 1;
        final int marginRight = 1;
        final int messageSpacing = 2;
        final int lineHeight = 9;

        final int maxChatHeight;
        final boolean isChatOpen;
        if (this.minecraft.screen instanceof ChatScreen) {
            maxChatHeight = 200;
            isChatOpen = true;
        } else {
            maxChatHeight = 100;
            isChatOpen = false;
        }

        int chatHeight = 0;

        for (AC_ChatMessage message : messages) {
            if (message.age >= 200 && !isChatOpen) {
                continue;
            }
            int alpha = this.getMessageAlpha(message, isChatOpen);
            if (alpha <= 0) {
                continue;
            }

            for (int i = message.lines.size() - 1; i >= 0; i--) {
                chatHeight += lineHeight;
                if (chatHeight >= maxChatHeight) {
                    break;
                }
            }

            if (chatHeight >= maxChatHeight) {
                break;
            }
            chatHeight += messageSpacing;
        }

        GL11.glEnable(GL11.GL_BLEND);

        int x = 2;
        int yOffset = 0;

        TextRendererState textState = exTextRenderer.createState();
        textState.bindTexture();
        textState.setShadowOffset(1, 1);

        for (AC_ChatMessage message : messages) {
            if (message.age >= 200 && !isChatOpen) {
                continue;
            }
            int alpha = this.getMessageAlpha(message, isChatOpen);
            if (alpha <= 0) {
                continue;
            }

            String text = message.text;
            int color = Rgba.withAlpha(0xffffff, alpha);
            textState.setColor(color);
            textState.setShadow(ExTextRenderer.getShadowColor(color));

            int xStart = x - marginLeft;
            int xEnd = x + CHAT_WIDTH + marginRight;

            int msgHeight = message.lines.size() * lineHeight;
            int yBase = (screenHeight - 48) - msgHeight - yOffset;
            int yEnd = yBase + msgHeight + 1;
            int y = yBase + 1;

            this.fill(xStart, yBase, xEnd, yEnd, alpha / 2 << 24);
            GL11.glEnable(GL11.GL_BLEND);

            var ts = Tesselator.instance;
            textState.begin(ts);
            for (AC_ChatMessage.Line line : message.lines) {
                textState.drawText(ts, text, line.start(), line.end(), x, y);

                y += lineHeight;
                yOffset += lineHeight;
                if (yOffset >= maxChatHeight) {
                    break;
                }
            }
            textState.end(ts);

            if (yOffset >= maxChatHeight) {
                break;
            }
            yOffset += messageSpacing;
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    private int getMessageAlpha(AC_ChatMessage message, boolean isChatOpen) {
        if (isChatOpen) {
            return 255;
        }

        double age = (double) message.age / 200.0D;
        age = 1.0D - age;
        age *= 10.0D;
        if (age < 0.0D) {
            age = 0.0D;
        }

        if (age > 1.0D) {
            age = 1.0D;
        }

        age *= age;

        int alpha = (int) (255.0D * age);
        return alpha;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void runTick(CallbackInfo ci) {
        for (AC_ChatMessage message : this.chatMessages) {
            message.age += 1;
        }
    }

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void clearChat(CallbackInfo ci) {
        chatMessages.clear();
    }

    @Overwrite
    public void addMessage(String message) {
        ACMod.CHAT_LOGGER.info(colorCodesToAnsi(message, 0, message.length()).toString());

        var entry = new AC_ChatMessage(message);
        entry.rebuild((ExTextRenderer) this.minecraft.font, CHAT_WIDTH);
        this.chatMessages.addFirst(entry);

        int bufferLimit = ((ExGameOptions) minecraft.options).getChatMessageBufferLimit();
        while (this.chatMessages.size() > bufferLimit) {
            this.chatMessages.removeLast();
        }
    }

    private static StringBuilder colorCodesToAnsi(CharSequence text, int start, int end) {
        TextRendererState.validateCharSequence(text, start, end);
        var builder = new StringBuilder((int) ((end - start) * 1.1));
        for (int i = start; i < end; ++i) {
            char c = text.charAt(i);
            if (end > i + 1 && c == '§') {
                int colorIndex = HexConvert.fromHexDigit(text.charAt(i + 1));
                if (colorIndex < 0 || colorIndex > 15) {
                    colorIndex = 15;
                }

                String sequence = CODE_TO_ANSI_SEQUENCE[colorIndex];
                builder.append(sequence);
                i++;
                continue;
            }
            builder.append(c);
        }
        return builder;
    }

    private void renderOverlay(int x, int y, String name) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textures.loadTexture("/overlays/" + name));
        Tesselator ts = Tesselator.instance;
        ts.begin();
        ts.vertexUV(0.0D, y, -90.0D, 0.0D, 1.0D);
        ts.vertexUV(x, y, -90.0D, 1.0D, 1.0D);
        ts.vertexUV(x, 0.0D, -90.0D, 1.0D, 0.0D);
        ts.vertexUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        ts.end();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public ScriptUIContainer getScriptUI() {
        return this.scriptUI;
    }

    @Override
    public boolean getHudEnabled() {
        return this.hudEnabled;
    }

    @Override
    public void setHudEnabled(boolean value) {
        this.hudEnabled = value;
    }
}
