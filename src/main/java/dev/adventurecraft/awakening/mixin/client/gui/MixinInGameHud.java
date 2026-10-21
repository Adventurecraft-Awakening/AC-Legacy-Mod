package dev.adventurecraft.awakening.mixin.client.gui;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.UiDrawContext;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.resource.RenderDevice;
import dev.adventurecraft.awakening.client.gui.AC_ChatScreen;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.dom.Node;
import dev.adventurecraft.awakening.dom.Style;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.entity.EntityClass;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Border;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.layout.Rect;
import dev.adventurecraft.awakening.util.FacingUtil;
import dev.adventurecraft.awakening.util.HexConvert;
import dev.adventurecraft.awakening.script.ScriptUIContainer;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.HitType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Random;

@Mixin(Gui.class)
public abstract class MixinInGameHud extends GuiComponent implements ExInGameHud {

    @Unique private static final String[] CODE_TO_ANSI_SEQUENCE = new String[] {
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

    @Unique private static final long MAX_MESSAGE_AGE = 200 * 50;

    @Shadow private static net.minecraft.client.renderer.entity.ItemRenderer itemRenderer;
    @Shadow float tbr;
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
        dev.adventurecraft.awakening.client.render.RenderSystem.phase(dev.adventurecraft.awakening.client.render.RenderPhase.UI);
        final Minecraft mc = this.minecraft;
        final LocalPlayer player = mc.player;
        final Font font = mc.font;

        var scaler = new ScreenSizeCalculator(mc.options, mc.width, mc.height);
        final int screenWidth = scaler.getWidth();
        final int screenHeight = scaler.getHeight();

        var barRect = new IntRect(screenWidth / 2 - 91, screenHeight - 22, 182, 22);

        mc.gameRenderer.setScreenProjectionMatrix();
        try (var ui = LegacyPassBridge.uiContext(font, mc.textures)) {
            RenderTexture hudTexture;
            if (Minecraft.useFancyGraphics()) {
                this.ac$vignette(ui,player.getBrightness(partialTick),screenWidth,screenHeight);
            }

            if (!mc.options.thirdPersonView && !((ExMinecraft) mc).isCameraActive()) {
                ItemInstance headItem = player.inventory.getArmor(3);
                if (headItem != null && headItem.id == Tile.PUMPKIN.id) {
                    ui.image(ui.texture("%blur%/misc/pumpkinblur.png"), new Rect(0,0,screenWidth,screenHeight),new Rect(0,0,1,1),new dev.adventurecraft.awakening.layout.IntCorner(-1));
                }
            }

            if (mc.level != null) {
                String overlay = ((ExWorldProperties) mc.level.levelData).getOverlay();
                if (!overlay.isEmpty()) {
                    this.renderOverlay(ui,screenWidth,screenHeight,overlay);
                }
            }

            float portalTime = MathF.fastLerp(partialTick, player.oPortalTime, player.portalTime);
            if (portalTime > 0.0F) {
                this.ac$portal(ui,portalTime,screenWidth,screenHeight);
            }

            if (mc.level != null) {
                // Refresh hudEnabled property (has to be here, because ui.hudEnabled can be set directly....)
                ((ExWorldProperties) mc.level.levelData).setHudEnabled(this.hudEnabled);
            }

            final int slotWidth = 20;

            if (this.hudEnabled) {
                final int maxHealth = ((ExMob) player).getMaxHealth();
                final int heartRows = (maxHealth - 1) / 40;

                hudTexture=ui.texture("/gui/gui.png");
                Inventory inv = player.inventory;
                this.blitOffset = -90.0F;
                // Render bar
                this.ac$blit(ui,hudTexture,barRect.left(), barRect.top(), 0, 0, barRect.width(), barRect.height());

                // Render off-hand slot outline.
                int offhandLeft = barRect.left() - 1 + ((ExPlayerInventory) inv).getOffhandSlot() * slotWidth;
                this.ac$blit(ui,hudTexture,offhandLeft, barRect.top() - 1, 24, 22, 48, barRect.height());

                // Render main-hand slot outline.
                int mainhandLeft = barRect.left() - 1 + inv.selected * slotWidth;
                this.ac$blit(ui,hudTexture,mainhandLeft, barRect.top() - 1, 0, 22, 24, barRect.height());

                hudTexture=ui.texture("/gui/icons.png");

                // Render cross-hair
                var crosshair=ui.withParameters(MaterialRecipe.UI_INVERT.apply(ui.parameters()));
                this.ac$blit(crosshair,hudTexture,screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);
                var icons=ui.withParameters(MaterialRecipe.UI_CUTOUT.apply(ui.parameters()));

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
                            this.ac$blit(icons,hudTexture,armorLeft, y, u, 9, 9, 9);
                        }

                        int heartLeft = barRect.left() + armorIndex * 8;
                        if (playerHealth <= lowHealthThreshold) {
                            y += this.random.nextInt(2);
                        }

                        for (int healthIndex = 0; healthIndex <= heartRows; ++healthIndex) {
                            if ((armorIndex + 1 + healthIndex * 10) * 4 <= maxHealth) {
                                int u0 = isInvulnerable ? 9 : 0;

                                this.ac$blit(icons,hudTexture,heartLeft, y, 16 + u0, 0, 9, 9);

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
                                        this.ac$blit(icons,hudTexture,heartLeft, y, u1, 0, 9, 9);
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
                                    this.ac$blit(icons,hudTexture,heartLeft, y, u2, 0, 9, 9);
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
                        this.ac$blit(icons,hudTexture,x, y, u, 18, 9, 9);
                    }
                }
            }

            if (this.hudEnabled) {

                for (int slot = 0; slot < 9; ++slot) {
                    int x = barRect.left() + slot * slotWidth + 3;
                    int y = barRect.top() + 3;
                    var stack=player.inventory.items[slot];
                    if(stack==null) continue;
                    var preview=ui;
                    float pop=stack.popTime-partialTick;
                    if(pop>0) {
                        float stretch=1+pop/5;
                        var model=ui.parameters().object().model().translated(x+8,y+12,0)
                            .scaled(1/stretch,(stretch+1)/2,1).translated(-x-8,-y-12,0);
                        preview=ui.withParameters(ui.parameters().withObject(ui.parameters().object().withModel(model)));
                    }
                    ((dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer)itemRenderer).renderGuiItem(preview,stack,x,y);
                    dev.adventurecraft.awakening.client.render.ItemPreviewRenderer.decorations(ui,stack,x,y);
                }

            }

            int sleepTimer = player.getSleepTimer();
            if (sleepTimer > 0) {

                float sleepFactor = (float) sleepTimer / 100.0F;
                if (sleepFactor > 1.0F) {
                    sleepFactor = 1.0F - (float) (sleepTimer - 100) / 10.0F;
                }

                int color = Rgba.withAlpha(0x101020, (int) (220.0F * sleepFactor));
                ui.fill(new Rect(0,0,screenWidth,screenHeight),color);

            }

            int x = 2;
            final int color0 = 0xffffff;
            final int color1 = 0xe0e0e0;

            if (mc.options.renderDebug) {
                int y = 2;
                if (Minecraft.sessionTime > 0L) {
                    y += 32;
                }

                var textState = ((ExTextRenderer) font).createState();
                textState.setShadowOffset(1, 1);

                textState.setColor(Rgba.withAlpha(color0, 0xff));
                textState.setShadowToColor();
                textState.begin(ui);

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

                RenderDevice.DeviceInfo renderDeviceInfo = ((ExMinecraft) mc).getRenderDevice().getDeviceInfo();
                long terrainBytes = renderDeviceInfo.bufferAllocatedBytes();
                long terrainMeshes = renderDeviceInfo.bufferCount();
                String terrainMsg = "Vulkan terrain: %d meshes | %dMB".formatted(terrainMeshes, terrainBytes / 1024L / 1024L);
                textState.drawText(terrainMsg, screenWidth - font.width(terrainMsg) - 2, y1 += 10);

                textState.drawText("XYZ: %.3f / %.5f / %.3f".formatted(player.x, player.y, player.z), x, y += 8);
                int facing = FacingUtil.getFacing(player.yRot);
                String facingName = FacingUtil.getFacingName(facing).toLowerCase();
                String facingText = "Facing: %s (%s) (%.1f / %.1f)".formatted(
                    facingName,
                    FacingUtil.getFacingDir(facing),
                    player.yRot,
                    player.xRot
                );
                textState.drawText(facingText, x, y += 8);
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
                var collideMsg = String.format("Collide XZ: %d / %d", exPlayer.getCollisionX(), exPlayer.getCollisionZ());
                textState.drawText(collideMsg, x, y += 10);
                y += 10;

                var hit = mc.hitResult;
                if (hit != null) {
                    String targetMsg = "";
                    String hitMsg = "";
                    if (hit.hitType == HitType.TILE) {
                        int id = mc.level.getTile(hit.x, hit.y, hit.z);
                        int meta = mc.level.getData(hit.x, hit.y, hit.z);
                        // TODO: better tile names (BlockState)
                        Tile tile = Tile.tiles[id];
                        String name = tile != null ? tile.getName() : "<noname>";

                        targetMsg = String.format("Target Block: %d %d %d", hit.x, hit.y, hit.z);
                        hitMsg = String.format("%d:%d (%s)", id, meta, name);
                    }
                    else if (hit.hitType == HitType.ENTITY) {
                        Entity e = hit.entity;
                        targetMsg = String.format("Target Entity: %.1f %.1f %.1f", e.x, e.y, e.z);
                        hitMsg = ((EntityClass) e).getClassType() + "#" + Integer.toHexString(e.id);
                    }
                    textState.drawText(targetMsg, x, y += 10);
                    textState.drawText(hitMsg, x, y += 10);
                }

                textState.end();
            }
            else {
                int y = 2; // 12 prev
                if (EditorSession.current().active) {
                    this.ac$text(ui,AC_Version.shortVersion, x, y, color0);
                    y += 10;

                    var gitMeta = ACMod.GIT_META;
                    if (gitMeta != null && !gitMeta.isCleanTag) {
                        String branchHash = "Branch \"" + gitMeta.branch + "\" - " + gitMeta.version;
                        this.ac$text(ui,branchHash, x, y, color0);
                        y += 10;
                    }

                    this.ac$text(ui,"Debug Active", x, y, color0);
                    y += 10;
                }

                if (EditorSession.current().levelEditing) {
                    this.ac$text(ui,"Map Editing", x, y, color0);
                }
            }

            if (this.nowPlayingTime > 0) {
                float partialTime = (float) this.nowPlayingTime - partialTick;
                int alpha = Math.min((int) (partialTime * 256.0F / 20.0F), 255);
                if (alpha > 0) {
                    int color = color0;
                    if (this.animateOverlayMessageColor) {
                        color = java.awt.Color.HSBtoRGB(partialTime / 50.0F, 0.7F, 0.6F) & color0;
                    }

                    int pX = screenWidth / 2 - font.width(this.nowPlayingString) / 2;
                    ui.text(this.nowPlayingString,pX,screenHeight-48-4,Rgba.withAlpha(color,alpha),false);
                }
            }

            this.scriptUI.render(ui,partialTick);

            this.renderChat(ui,screenWidth,screenHeight);
        }
    }

    @Unique private void ac$blit(UiDrawContext ui,RenderTexture texture,int x,int y,int u,int v,int width,int height) {
        ui.image(texture,new Rect(x,y,width,height),new Rect(u/256.0,v/256.0,width/256.0,height/256.0),
            new dev.adventurecraft.awakening.layout.IntCorner(-1));
    }
    @Unique private void ac$text(UiDrawContext ui,String text,int x,int y,int color) {
        ui.text(text,x,y,Rgba.withAlpha(color,255),true);
    }
    @Unique private void ac$vignette(UiDrawContext ui,float brightness,int width,int height) {
        float target=Math.clamp(1-brightness,0,1); this.tbr+=(target-this.tbr)*.01f;
        var darken=ui.withParameters(ui.parameters().withRaster(ui.parameters().raster().withBlend(
            new BlendState(true,BlendFactor.ZERO,BlendFactor.ONE_MINUS_SRC_COLOR,Color4.TRANSPARENT))));
        int shade=Rgba.saturateToByte(this.tbr);
        darken.image(ui.texture("%blur%/misc/vignette.png"),new Rect(0,0,width,height),new Rect(0,0,1,1),
            new dev.adventurecraft.awakening.layout.IntCorner(Rgba.fromRgba8(shade,shade,shade,255)));
    }
    @Unique private void ac$portal(UiDrawContext ui,float alpha,int width,int height) {
        if(alpha<1) { alpha*=alpha;alpha*=alpha;alpha=alpha*.8f+.2f; }
        int tile=Tile.PORTAL.tex;
        ui.image(ui.texture("/terrain.png"),new Rect(0,0,width,height),new Rect((tile%16)/16.0,(tile/16)/16.0,1.0/16,1.0/16),
            new dev.adventurecraft.awakening.layout.IntCorner(Rgba.withAlpha(-1,Rgba.saturateToByte(alpha))));
    }

    @Unique
    private void renderChat(UiDrawContext ui,int screenWidth,int screenHeight) {
        final Font font = this.minecraft.font;
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
            if (freeLines <= 0) {
                break;
            }

            if (message.maxWidth != this.chatWidth) {
                message.rebuild(font, this.chatWidth);
            }

            int usedLines = Math.min(freeLines, message.lines.size());
            int msgHeight = usedLines * lineHeight;
            int y = chatY - chatHeight - msgHeight;

            var rect = new Rect(x, y, chatWidth, msgHeight).expand(shadowBorder);
            ui.fill(rect,Rgba.withAlpha(0,alpha/2));

            chatHeight += msgHeight + messageSpacing;
        }


        int stateHeight = 0;

        TextRendererState textState = ((ExTextRenderer) font).createState();
        textState.setShadowOffset(1, 1);

        textState.begin(ui);
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
            if (freeLines <= 0) {
                break;
            }

            var lines = message.lines;
            int totalLines = lines.size();
            int usedLines = Math.min(freeLines, totalLines);
            int startLine = totalLines - usedLines;

            int msgHeight = usedLines * lineHeight;
            int y = chatY - stateHeight - msgHeight;

            int color = Rgba.withAlpha(0xffffff, alpha);
            textState.setColor(color);
            textState.setShadowToColor();
            textState.resetFormat();

            // Apply formatting of skipped lines.
            for (int i = 0; i < startLine; i++) {
                var content = lines.get(i).content();
                textState.formatText(content, 0, content.length());
            }

            for (int i = startLine; i < totalLines; i++) {
                var content = lines.get(i).content();
                textState.drawText(content, 0, content.length(), x, y);
                y += lineHeight;
            }

            stateHeight += msgHeight + messageSpacing;
        }
        textState.end();

    }

    @Unique
    private int getMessageAlpha(long ageMillis, boolean isChatOpen) {
        if (isChatOpen) {
            return 255;
        }

        double age = 1.0D - (double) ageMillis / MAX_MESSAGE_AGE;
        age = MathF.saturate(10.0D * age);
        age *= age;

        return (int) (255.0D * age);
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
        this.addMessage(Node.text(message));
    }

    @Override
    public void addMessage(Node node) {
        var builder = new StringBuilder();
        node.visit(
            (content, style) -> {
                // TODO: apply remaining style options
                Integer color = style.getColor();
                if (color != null) {
                    builder
                        .append("\033[38;2;")
                        .append(Rgba.red(color))
                        .append(';')
                        .append(Rgba.green(color))
                        .append(';')
                        .append(Rgba.blue(color))
                        .append('m');
                }
                // TODO: do color-code conversion at higher levels to avoid overhead here
                colorCodesToAnsi(content, 0, content.length(), builder);
                builder.append("\033[0m");
                return Optional.empty();
            }, Style.EMPTY
        );
        ACMod.CHAT_LOGGER.info(builder.toString());

        var entry = new AC_ChatMessage(node, System.currentTimeMillis());
        entry.rebuild(this.minecraft.font, this.chatWidth);
        this.chatMessages.addFirst(entry);

        int bufferLimit = ((ExGameOptions) minecraft.options).getChatMessageBufferLimit();
        while (this.chatMessages.size() > bufferLimit) {
            this.chatMessages.removeLast();
        }
    }

    @Unique
    private static void colorCodesToAnsi(CharSequence text, int start, int end, StringBuilder builder) {
        TextRendererState.validateCharSequence(text, start, end);
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
    }

    @Unique
    private void renderOverlay(UiDrawContext ui,int x,int y,String name) {
        ui.image(ui.texture("/overlays/"+name),new Rect(0,0,x,y),new Rect(0,0,1,1),new dev.adventurecraft.awakening.layout.IntCorner(-1));
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
