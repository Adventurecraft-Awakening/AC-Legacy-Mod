package dev.adventurecraft.awakening.mixin.client.gui;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.gl.GLDevice;
import dev.adventurecraft.awakening.client.gui.AC_ChatScreen;
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
import dev.adventurecraft.awakening.layout.Border;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.layout.Rect;
import dev.adventurecraft.awakening.util.DrawUtil;
import dev.adventurecraft.awakening.util.HexConvert;
import dev.adventurecraft.awakening.script.ScriptUIContainer;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Random;

@Mixin(Gui.class)
public abstract class MixinInGameHud extends GuiComponent implements ExInGameHud {

    private static final String[] CODE_TO_ANSI_SEQUENCE = new String[] {
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

    private static final long MAX_MESSAGE_AGE = 200 * 50;

    @Shadow private Random random;
    @Shadow private Minecraft minecraft;
    @Shadow private int tickCount;
    @Shadow private String nowPlayingString;
    @Shadow private int nowPlayingTime;
    @Shadow private boolean animateOverlayMessageColor;

    @Shadow
    protected abstract void renderPumpkin(int i, int j);

    @Shadow
    protected abstract void renderPortalOverlay(float f, int i, int j);

    @Shadow
    protected abstract void renderSlot(int i, int j, int k, float f);

    @Shadow
    protected abstract void renderVignette(float f, int i, int j);

    @Unique private ArrayDeque<AC_ChatMessage> chatMessages;
    @Unique public ScriptUIContainer scriptUI;
    @Unique public boolean hudEnabled = true;
    @Unique private int chatWidth;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(Minecraft var1, CallbackInfo ci) {
        this.chatMessages = new ArrayDeque<>();
        this.scriptUI = new ScriptUIContainer(0.0F, 0.0F, null);
    }

    @Overwrite
    public void render(float partialTick, boolean hasScreen, int mouseX, int mouseY) {
        final Minecraft mc = this.minecraft;
        final LocalPlayer player = mc.player;
        final Font font = mc.font;

        var scaler = new ScreenSizeCalculator(mc.options, mc.width, mc.height);
        final int screenWidth = scaler.getWidth();
        final int screenHeight = scaler.getHeight();

        var barRect = new IntRect(screenWidth / 2 - 91, screenHeight - 22, 182, 22);

        mc.gameRenderer.setScreenProjectionMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(player.getBrightness(partialTick), screenWidth, screenHeight);
        }

        if (!mc.options.thirdPersonView && !((ExMinecraft) mc).isCameraActive()) {
            ItemInstance headItem = player.inventory.getArmor(3);
            if (headItem != null && headItem.id == Tile.PUMPKIN.id) {
                this.renderPumpkin(screenWidth, screenHeight);
            }
        }

        if (mc.level != null) {
            String overlay = ((ExWorldProperties) mc.level.levelData).getOverlay();
            if (!overlay.isEmpty()) {
                this.renderOverlay(screenWidth, screenHeight, overlay);
            }
        }

        float portalTime = MathF.fastLerp(partialTick, player.oPortalTime, player.portalTime);
        if (portalTime > 0.0F) {
            this.renderPortalOverlay(portalTime, screenWidth, screenHeight);
        }

        if (mc.level != null) {
            // Refresh hudEnabled property (has to be here, because ui.hudEnabled can be set directly....)
            ((ExWorldProperties) mc.level.levelData).setHudEnabled(this.hudEnabled);
        }

        final int slotWidth = 20;

        if (this.hudEnabled) {
            final int maxHealth = ((ExMob) player).getMaxHealth();
            final int heartRows = (maxHealth - 1) / 40;

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.textures.loadTexture("/gui/gui.png"));
            Inventory inv = player.inventory;
            this.blitOffset = -90.0F;
            // Render bar
            this.blit(barRect.left(), barRect.top(), 0, 0, barRect.width(), barRect.height());

            // Render off-hand slot outline.
            int offhandLeft = barRect.left() - 1 + ((ExPlayerInventory) inv).getOffhandSlot() * slotWidth;
            this.blit(offhandLeft, barRect.top() - 1, 24, 22, 48, barRect.height());

            // Render main-hand slot outline.
            int mainhandLeft = barRect.left() - 1 + inv.selected * slotWidth;
            this.blit(mainhandLeft, barRect.top() - 1, 0, 22, 24, barRect.height());

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.textures.loadTexture("/gui/icons.png"));

            // Render cross-hair
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
            this.blit(screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);

            final int lowHealthThreshold = 8;
            final int playerHealth = player.health;
            final int playerPrevHealth = player.lastHealth;
            this.random.setSeed(this.tickCount * 312871L);

            final int heartBot = barRect.top() - 10;
            final int heartTop = heartBot - 9 * heartRows;

            if (mc.gameMode.canHurtPlayer()) {
                int playerArmor = player.getArmor();
                boolean isInvulnerable = player.invulnerableTime >= 10 && player.invulnerableTime / 3 % 2 == 1;

                for (int armorIndex = 0; armorIndex < 10; ++armorIndex) {
                    int y = heartBot;
                    if (playerArmor > 0) {
                        int armorLeft = barRect.right() - armorIndex * 8 - 9;
                        int u = 25 - Integer.compare((armorIndex * 2 + 1), playerArmor) * 9;
                        this.blit(armorLeft, y, u, 9, 9, 9);
                    }

                    int heartLeft = barRect.left() + armorIndex * 8;
                    if (playerHealth <= lowHealthThreshold) {
                        y += this.random.nextInt(2);
                    }

                    for (int healthIndex = 0; healthIndex <= heartRows; ++healthIndex) {
                        if ((armorIndex + 1 + healthIndex * 10) * 4 <= maxHealth) {
                            int u0 = isInvulnerable ? 9 : 0;

                            this.blit(heartLeft, y, 16 + u0, 0, 9, 9);

                            if (isInvulnerable) {
                                int u1 = -1;
                                if (armorIndex * 4 + 3 + healthIndex * 40 < playerPrevHealth) {
                                    u1 = 70;
                                }
                                else if (armorIndex * 4 + 3 + healthIndex * 40 == playerPrevHealth) {
                                    u1 = 105;
                                }
                                else if (armorIndex * 4 + 2 + healthIndex * 40 == playerPrevHealth) {
                                    u1 = 79;
                                }
                                else if (armorIndex * 4 + 1 + healthIndex * 40 == playerPrevHealth) {
                                    u1 = 114;
                                }
                                if (u1 != -1) {
                                    this.blit(heartLeft, y, u1, 0, 9, 9);
                                }
                            }

                            int u2 = -1;
                            if (armorIndex * 4 + 3 + healthIndex * 40 < playerHealth) {
                                u2 = 52;
                            }
                            else if (armorIndex * 4 + 3 + healthIndex * 40 == playerHealth) {
                                u2 = 87;
                            }
                            else if (armorIndex * 4 + 2 + healthIndex * 40 == playerHealth) {
                                u2 = 61;
                            }
                            else if (armorIndex * 4 + 1 + healthIndex * 40 == playerHealth) {
                                u2 = 96;
                            }
                            if (u2 != -1) {
                                this.blit(heartLeft, y, u2, 0, 9, 9);
                            }
                        }
                        y -= 9;
                    }
                }
            }

            if (player.isUnderLiquid(Material.WATER)) {
                int airAlpha = (int) Math.ceil((player.airSupply - 2) * 10.0D / 300.0D);
                int airUsed = (int) Math.ceil(player.airSupply * 10.0D / 300.0D) - airAlpha;

                for (int bubbleIndex = 0; bubbleIndex < airAlpha + airUsed; ++bubbleIndex) {
                    int u = bubbleIndex < airAlpha ? 16 : 25;
                    int x = barRect.left() + bubbleIndex * 8;
                    int y = heartTop - 9;
                    this.blit(x, y, u, 18, 9, 9);
                }
            }
        }
        else {
            GL11.glDisable(GL11.GL_BLEND);
        }

        if (this.hudEnabled) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            Lighting.turnOn();
            GL11.glPopMatrix();

            for (int slot = 0; slot < 9; ++slot) {
                int x = barRect.left() + slot * slotWidth + 3;
                int y = barRect.top() + 3;
                this.renderSlot(slot, x, y, partialTick);
            }

            Lighting.turnOff();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        int sleepTimer = player.getSleepTimer();
        if (sleepTimer > 0) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            float sleepFactor = (float) sleepTimer / 100.0F;
            if (sleepFactor > 1.0F) {
                sleepFactor = 1.0F - (float) (sleepTimer - 100) / 10.0F;
            }

            int color = Rgba.withAlpha(0x101020, (int) (220.0F * sleepFactor));
            this.fill(0, 0, screenWidth, screenHeight, color);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);

        int x = 2;
        final int color0 = 0xffffff;
        final int color1 = 0xe0e0e0;

        if (mc.options.renderDebug) {
            int y = 2;
            if (Minecraft.sessionTime > 0L) {
                y += 32;
            }

            var ts = Tesselator.instance;
            var textState = ((ExTextRenderer) font).createState();
            textState.setShadowOffset(1, 1);

            textState.setColor(Rgba.withAlpha(color0, 0xff));
            textState.setShadowToColor();
            textState.begin(ts);

            textState.drawText(AC_Version.shortVersion, x, y);
            textState.drawText(mc.fpsString, x, y += 10);
            textState.drawText(mc.getChunkStatistics(), x, y += 10);
            textState.drawText(mc.getEntityStatistics(), x, y += 10);
            textState.drawText(mc.getParticleStatistics(), x, y += 10);
            textState.drawText(mc.getDebugInfo(), x, y += 10);
            y += 10;

            textState.setColor(Rgba.withAlpha(color1, 0xff));
            textState.setShadowToColor();
            textState.resetFormat();

            long maxMem = Runtime.getRuntime().maxMemory();
            long totMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long usedMem = totMem - freeMem;

            int y1 = 2;
            String usedMsg = "Used: %d%% (%dMB) of %dMB".formatted(
                usedMem * 100L / maxMem,
                usedMem / 1024L / 1024L,
                maxMem / 1024L / 1024L
            );
            textState.drawText(usedMsg, screenWidth - font.width(usedMsg) - 2, y1);

            String allocMsg = "Allocated: %d%% (%dMB)".formatted(totMem * 100L / maxMem, totMem / 1024L / 1024L);
            textState.drawText(allocMsg, screenWidth - font.width(allocMsg) - 2, y1 += 10);
            y1 += 10;

            GLDevice.DeviceInfo glDeviceInfo = ((ExMinecraft) mc).getGlDevice().getDeviceInfo();
            long gpuBufMem = glDeviceInfo.bufferAllocatedBytes();
            long gpuBufCount = glDeviceInfo.bufferCount();
            String gpuBufMsg = "GL Buffers: %d | %dMB".formatted(gpuBufCount, gpuBufMem / 1024L / 1024L);
            textState.drawText(gpuBufMsg, screenWidth - font.width(gpuBufMsg) - 2, y1 += 10);

            textState.drawText("x: " + player.x, x, y += 8);
            textState.drawText("y: " + player.y, x, y += 8);
            textState.drawText("z: " + player.z, x, y += 8);
            int facing = ((int) Math.floor((player.yRot * 4.0F / 360.0F) + 0.5D) & 3);
            textState.drawText("f: " + facing, x, y += 8);
            y += 10;

            boolean useWorldGenImages = ((ExWorldProperties) mc.level.levelData).getWorldGenProps().useImages;
            if (useWorldGenImages) {
                int pX = (int) player.x;
                int pY = (int) player.z;
                int tH = AC_TerrainImage.getTerrainHeight(pX, pY);
                int wH = AC_TerrainImage.getWaterHeight(pX, pY);
                float tTemp = AC_TerrainImage.getTerrainTemperature(pX, pY);
                float tHumid = AC_TerrainImage.getTerrainHumidity(pX, pY);
                String msg = String.format("T: %d W: %d Temp: %.2f Humid: %.2f", tH, wH, tTemp, tHumid);
                textState.drawText(msg, x, y += 10);
            }

            var exPlayer = (ExEntity) player;
            var collideMsg = String.format("Collide X: %d Z: %d", exPlayer.getCollisionX(), exPlayer.getCollisionZ());
            textState.drawText(collideMsg, x, y += 10);

            textState.end();
        }
        else {
            int y = 2; // 12 prev
            if (AC_DebugMode.active) {
                font.drawShadow(AC_Version.shortVersion, x, y, color0);
                y += 10;

                var gitMeta = ACMod.GIT_META;
                if (gitMeta != null && !Boolean.parseBoolean(gitMeta.isCleanTag)) {
                    String branchHash = "Branch \"" + gitMeta.branch + "\" - " + gitMeta.hash;
                    font.drawShadow(branchHash, x, y, color0);
                    y += 10;
                }

                font.drawShadow("Debug Active", x, y, color0);
                y += 10;
            }

            if (AC_DebugMode.levelEditing) {
                font.drawShadow("Map Editing", x, y, color0);
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (this.nowPlayingTime > 0) {
            float partialTime = (float) this.nowPlayingTime - partialTick;
            int alpha = Math.min((int) (partialTime * 256.0F / 20.0F), 255);
            if (alpha > 0) {
                int color = color0;
                if (this.animateOverlayMessageColor) {
                    color = Color.HSBtoRGB(partialTime / 50.0F, 0.7F, 0.6F) & color0;
                }

                int pX = screenWidth / 2 - font.width(this.nowPlayingString) / 2;
                font.draw(this.nowPlayingString, pX, screenHeight - 48 - 4, Rgba.withAlpha(color, alpha));
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        this.scriptUI.render(font, mc.textures, partialTick);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        this.renderChat(screenWidth, screenHeight);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    private void renderChat(int screenWidth, int screenHeight) {
        final var ts = Tesselator.instance;
        final var exFont = (ExTextRenderer) this.minecraft.font;
        final ArrayDeque<AC_ChatMessage> messages = this.chatMessages;

        var shadowBorder = new Border(1, 1, 1, 0);
        final int messageSpacing = 2;
        final int lineHeight = 9;

        final int maxChatHeight;
        final boolean isChatOpen;
        if (this.minecraft.screen instanceof AC_ChatScreen) {
            maxChatHeight = screenHeight - 48 * 3;
            isChatOpen = true;
        }
        else {
            maxChatHeight = 100;
            isChatOpen = false;
        }

        this.chatWidth = Math.round(((ExGameOptions) this.minecraft.options).getChatWidth() * screenWidth);

        int chatHeight = 0;
        int chatY = screenHeight - 48;
        int x = 2;

        GL11.glEnable(GL11.GL_BLEND);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        ts.begin();
        for (AC_ChatMessage message : messages) {
            long age = message.getAgeInMillis();
            if (age >= MAX_MESSAGE_AGE && !isChatOpen) {
                continue;
            }
            int alpha = this.getMessageAlpha(age, isChatOpen);
            if (alpha <= 0) {
                continue;
            }

            int freeHeight = maxChatHeight - chatHeight;
            int freeLines = freeHeight / lineHeight;
            if (freeLines == 0) {
                break;
            }

            if (message.maxWidth != this.chatWidth) {
                message.rebuild(exFont, this.chatWidth);
            }

            int usedLines = Math.min(freeLines, message.lines.size());
            int msgHeight = usedLines * lineHeight;
            int y = chatY - chatHeight - msgHeight;

            var rect = new Rect(x, y, chatWidth, msgHeight).expand(shadowBorder);
            DrawUtil.fillRect(ts, rect, Rgba.withAlpha(0, alpha / 2));

            chatHeight += msgHeight + messageSpacing;
        }
        ts.end();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        int stateHeight = 0;

        TextRendererState textState = exFont.createState();
        textState.setShadowOffset(1, 1);

        textState.begin(ts);
        for (AC_ChatMessage message : messages) {
            long age = message.getAgeInMillis();
            if (age >= MAX_MESSAGE_AGE && !isChatOpen) {
                continue;
            }
            int alpha = this.getMessageAlpha(age, isChatOpen);
            if (alpha <= 0) {
                continue;
            }

            int freeHeight = maxChatHeight - stateHeight;
            int freeLines = freeHeight / lineHeight;
            if (freeLines == 0) {
                break;
            }

            int usedLines = Math.min(freeLines, message.lines.size());
            int msgHeight = usedLines * lineHeight;
            int y = chatY - stateHeight - msgHeight;

            String text = message.text;
            int color = Rgba.withAlpha(0xffffff, alpha);
            textState.setColor(color);
            textState.setShadowToColor();
            textState.resetFormat();

            int totalLines = message.lines.size();
            int startLine = totalLines - usedLines;

            // Apply formatting of skipped lines.
            for (int i = 0; i < startLine; i++) {
                var line = message.lines.get(i);
                textState.formatText(text, line.start(), line.end());
            }

            for (int i = startLine; i < totalLines; i++) {
                var line = message.lines.get(i);
                textState.drawText(text, line.start(), line.end(), x, y);
                y += lineHeight;
            }

            stateHeight += msgHeight + messageSpacing;
        }
        textState.end();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private int getMessageAlpha(long ageMillis, boolean isChatOpen) {
        if (isChatOpen) {
            return 255;
        }

        double age = (double) ageMillis / MAX_MESSAGE_AGE;
        age = 1.0D - age;
        age *= 10.0D;
        age = MathF.clamp(age, 0.0, 1.0);
        age *= age;

        int alpha = (int) (255.0D * age);
        return alpha;
    }

    @Inject(
        method = "clearMessages",
        at = @At("HEAD")
    )
    private void clearChat(CallbackInfo ci) {
        this.chatMessages.clear();
    }

    @Overwrite
    public void addMessage(String message) {
        ACMod.CHAT_LOGGER.info(colorCodesToAnsi(message, 0, message.length()).toString());

        var entry = new AC_ChatMessage(message, System.currentTimeMillis());
        entry.rebuild((ExTextRenderer) this.minecraft.font, this.chatWidth);
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
