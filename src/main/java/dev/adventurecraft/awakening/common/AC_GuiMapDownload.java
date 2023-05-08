package dev.adventurecraft.awakening.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.SwingUtilities;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import dev.adventurecraft.awakening.script.ScriptUIContainer;
import org.lwjgl.opengl.GL11;

public class AC_GuiMapDownload extends Screen {

    public static final String url = "http://www.adventurecraft.org/";
    public ScriptUIContainer ui = new ScriptUIContainer(0.0F, 26.0F, null);
    protected Screen parentScreen;
    private File mapDownloadFolder = new File("./mapDownloads/");
    private AC_GuiMapElement mouseOver;
    private ArrayList<AC_GuiMapElement> maps = new ArrayList<>();
    private boolean downloading = true;
    private int mapImagesDownloaded = 0;
    private int totalMaps = 0;
    private boolean downloadingMap = false;
    private String downloadingMapName;
    private int downloadedAmount;
    private int downloadSize;
    private String mapUrl;
    private String mapName;
    private AC_GuiMapElement downloadingMapElement;
    private boolean rightClickDown = false;
    private int mouseY;
    private int maxOffset;
    private int scrollBarX;
    private boolean scrolling = false;
    private Random rand;

    public AC_GuiMapDownload(Screen var1) {
        if (!this.mapDownloadFolder.exists()) {
            this.mapDownloadFolder.mkdirs();
        }

        this.rand = new Random();
        SwingUtilities.invokeLater(AC_GuiMapDownload.this::downloadAndLoadMapInfo);
        this.mouseOver = null;
        this.parentScreen = var1;
        this.mapName = null;
    }

    @Override
    public void initVanillaScreen() {
        this.buttons.add(new ButtonWidget(0, 2, 2, 50, 20, "Back"));
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            this.client.openScreen(this.parentScreen);
        }
    }

    @Override
    protected void keyPressed(char var1, int var2) {
        if (!this.downloading) {
            super.keyPressed(var1, var2);
        }
    }

    @Override
    protected void mouseClicked(int var1, int var2, int var3) {
        if (this.downloading) {
            return;
        }

        super.mouseClicked(var1, var2, var3);
        if (var3 != 0) {
            if (var3 == 1) {
                this.rightClickDown = true;
                this.mouseY = var2;
            }
            return;
        }

        if (var2 > 24) {
            AC_GuiMapElement var4 = this.getMapAtCoord(var1, var2);
            if (var4 != null) {
                if ((float) var2 - var4.curY - this.ui.curY > 85.0F) {
                    var4.ratingClicked((int) ((float) var1 - var4.curX - this.ui.curX), (int) ((float) var2 - var4.curY - this.ui.curY));
                } else {
                    this.mapUrl = var4.mapURL;
                    this.mapName = var4.mapName;
                    this.downloadingMapElement = var4;
                    this.downloadSize = 0;
                    this.downloading = true;
                    this.downloadingMap = true;
                    this.downloadingMapName = this.mapName;
                    SwingUtilities.invokeLater(AC_GuiMapDownload.this::downloadMap);
                }
            }
        }

        if (this.maxOffset < 26 && var1 >= this.scrollBarX && var1 <= this.scrollBarX + 8 && var2 > 26) {
            float var5 = Math.max(Math.min((float) (var2 - 8 - 26) / ((float) this.height - 26.0F - 16.0F - 32.0F), 1.0F), 0.0F);
            this.ui.curY = (float) (26 - (int) (var5 * (float) (26 - this.maxOffset)));
            this.scrolling = true;
        }
    }

    @Override
    protected void mouseReleased(int var1, int var2, int var3) {
        if (this.downloading) {
            super.mouseReleased(var1, var2, var3);
            return;
        }

        if (this.rightClickDown && var2 != this.mouseY) {
            this.ui.curY += (float) (var2 - this.mouseY);
            this.mouseY = var2;
            if (this.ui.curY > 26.0F) {
                this.ui.curY = 26.0F;
            } else if (this.ui.curY < (float) this.maxOffset) {
                this.ui.curY = (float) this.maxOffset;
            }
        } else if (this.scrolling) {
            float var4 = Math.max(Math.min((float) (var2 - 8 - 26) / ((float) this.height - 26.0F - 16.0F - 32.0F), 1.0F), 0.0F);
            this.ui.curY = (float) (26 - (int) (var4 * (float) (26 - this.maxOffset)));
        }

        this.ui.onUpdate();
        if (var3 == 0) {
            this.scrolling = false;
        } else if (var3 == 1) {
            this.rightClickDown = false;
        }

        for (AC_GuiMapElement var5 : this.maps) {
            var5.mouseMoved((int) ((float) var1 - var5.curX - this.ui.curX), (int) ((float) var2 - var5.curY - this.ui.curY));
        }

        super.mouseReleased(var1, var2, var3);
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();

        float var5;
        String var10;
        if (!this.downloading) {
            if (this.mapName != null) {
                this.startMap();
                return;
            }

            this.fill(0, 24, this.width + 32, this.height + 32, 1073741824);
            this.fillGradient(0, 24, this.width + 32, 32, -16777216, 0);
            this.fillGradient(0, this.height - 32 - 8, this.width + 32, this.height - 32, 0, -16777216);
            AC_GuiMapElement var4 = this.getMapAtCoord(var1, var2);
            if (var4 != this.mouseOver) {
                if (var4 != null) {
                    var4.fadeDescriptionIn();
                }

                if (this.mouseOver != null) {
                    this.mouseOver.fadeDescriptionOut();
                }
            }

            this.mouseOver = var4;
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            this.ui.render(this.textRenderer, this.client.textureManager, var3);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            if (this.maxOffset < 26) {
                this.fill(this.scrollBarX, 26, this.scrollBarX + 8, this.height - 32, Integer.MIN_VALUE);
                var5 = 1.0F - (this.ui.curY - (float) this.maxOffset) / (26.0F - (float) this.maxOffset);
                int var6 = (int) ((float) (this.height - 26 - 16 - 32) * var5);
                this.fill(this.scrollBarX, 26 + var6, this.scrollBarX + 8, 26 + var6 + 16, -1325400065);
            }

            this.drawBackground(0, 24, 255, 255);
            this.drawBackground(this.height - 32, this.height, 255, 255);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int var9 = this.textRenderer.getTextWidth("Maps Available For Download");
            this.textRenderer.drawText("Maps Available For Download", this.width / 2 - var9 / 2, 8, 16777215);
            var10 = "Additional maps can be found on the AdventureCraft Wiki";
            var9 = this.textRenderer.getTextWidth(var10);
            this.textRenderer.drawText(var10, this.width / 2 - var9 / 2, this.height - 26, 16777215);
            var10 = "http://adventurecraft.wikkii.com/";
            var9 = this.textRenderer.getTextWidth(var10);
            this.textRenderer.drawText(var10, this.width / 2 - var9 / 2, this.height - 14, 16777215);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            super.render(var1, var2, var3);
        } else if (!this.downloadingMap) {
            this.drawStatus("Downloading Map Info", this.mapImagesDownloaded, this.totalMaps);
        } else {
            this.drawStatus("Downloading Map: " + this.mapName, this.downloadedAmount, this.downloadSize);
            if (this.downloadSize != 0) {
                float var8 = (float) this.downloadedAmount / 1024.0F / 1024.0F;
                var5 = (float) this.downloadSize / 1024.0F / 1024.0F;
                var10 = String.format("Downloaded %.2f/%.2f MBs", var8, var5);
                int var7 = this.textRenderer.getTextWidth(var10);
                this.textRenderer.drawText(var10, this.width / 2 - var7 / 2, this.height / 2 + 15, 16777215);
            }
        }
    }

    private void drawStatus(String var1, int var2, int var3) {
        int var4 = this.textRenderer.getTextWidth(var1);
        this.textRenderer.drawText(var1, this.width / 2 - var4 / 2, this.height / 2 - 4, 16777215);
        this.fill(this.width / 2 - 50, this.height / 2 + 5, this.width / 2 + 50, this.height / 2 + 13, Integer.MIN_VALUE);
        if (var3 > 0) {
            int var5 = (int) (100.0D * (double) var2 / (double) var3 - 50.0D);
            this.fill(this.width / 2 - 50, this.height / 2 + 5, this.width / 2 + var5, this.height / 2 + 13, -15675632);
        }
    }

    @Override
    public void init(Minecraft var1, int var2, int var3) {
        super.init(var1, var2, var3);
        this.ui.setX((float) (var2 / 2 - 152));
        this.scrollBarX = 1 + this.width / 2 + 152;
        this.maxOffset = Math.min(this.height - 32 - 102 * (this.maps.size() + 2) / 3, 26);
        if (this.ui.curY < (float) this.maxOffset) {
            this.ui.setY((float) this.maxOffset);
        }
    }

    private AC_GuiMapElement addMap(String var1, String var2, String var3, String var4, String var5, int var6, int var7, int var8) {
        int var9 = this.maps.size();
        var var10 = new AC_GuiMapElement(102 * (var9 % 3), 102 * (var9 / 3), this.ui, var1, var2, var3, var4, var5, var6, var7, var8);
        this.maps.add(var10);
        this.maxOffset = Math.min(this.height - 32 - 102 * (this.maps.size() + 2) / 3, 26);
        return var10;
    }

    private AC_GuiMapElement getMapAtCoord(int var1, int var2) {
        Iterator<AC_GuiMapElement> var3 = this.maps.iterator();

        AC_GuiMapElement var4;
        int var5;
        int var6;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            var4 = var3.next();
            var5 = (int) ((float) var1 - (var4.curX + this.ui.curX));
            var6 = (int) ((float) var2 - (var4.curY + this.ui.curY));
        } while (var5 < 0 || var5 >= 100 || var6 < 0 || var6 >= 100);

        return var4;
    }

    private void downloadAndLoadMapInfo() {
        File var1 = new File(this.mapDownloadFolder, "mapInfo.txt");
        if (!var1.exists()) {
            this.downloading = false;
            return;
        }

        try {
            BufferedReader var2 = new BufferedReader(new FileReader(var1));
            ArrayList<String> var3 = new ArrayList<>();

            while (var2.ready()) {
                String var4 = var2.readLine();
                var4 = var4.replace("\\n", "\n");
                var3.add(var4);
            }

            this.totalMaps = var3.size();

            for (String var5 : var3) {
                String[] var6 = var5.split(", ");
                if (var6.length < 13) {
                    ++this.mapImagesDownloaded;
                    continue;
                }

                int var7 = Integer.parseInt(var6[9]);
                String var8 = var6[0];
                String var9 = var6[4];
                String var10 = "http://www.adventurecraft.org/mapThumbnails/" + var6[0].replace(" ", "%20") + ".png";
                String var11 = var6[12];
                var10 = var10.replace(" ", "%20");
                boolean var12 = true;
                String var13 = "";
                if (!var6[2].equals("")) {
                    var13 = var13 + "by " + var6[2];
                    var12 = false;
                }

                if (!var6[8].equals("")) {
                    if (!var12) {
                        var13 = var13 + "\n";
                    }

                    var13 = var13 + "Downloads: " + var6[8];
                    var12 = false;
                }

                int var14 = Integer.parseInt(var6[10]);
                int var15 = Integer.parseInt(var6[11]);
                AC_GuiMapElement var16 = this.addMap(var8, var13, var9, "./mapDownloads/" + var8 + ".png", var11, var7, var14, var15);
                File var17 = new File("./maps/" + var16.mapName);
                if (var17.exists()) {
                    var16.setAsDownloaded();
                }
                ++this.mapImagesDownloaded;
            }
        } catch (FileNotFoundException var18) {
        } catch (IOException var19) {
        }

        this.downloading = false;
    }

    private void deleteFilesInFolder(File var1) {
        File[] var2 = var1.listFiles();

        for (File var5 : var2) {
            if (var5.isDirectory()) {
                this.deleteFilesInFolder(var5);
            }

            var5.delete();
        }
    }

    private void downloadMap() {
        String[] var1 = new String[]{"51083669", "51083634", "51083701", "51083780"};
        this.downloadFile(this.mapUrl.replace("51083780", var1[this.rand.nextInt(4)]), "./mapDownloads/map.zip");
        File var2 = new File(this.mapDownloadFolder, "map.zip");
        File var3 = new File("./maps/" + this.mapName);
        if (var3.exists()) {
            if (var3.isDirectory()) {
                this.deleteFilesInFolder(var3);
            }

            var3.delete();
        }

        var3.mkdir();
        ZipInputStream var4 = null;

        try {
            byte[] var6 = new byte[8192];
            var4 = new ZipInputStream(new FileInputStream(var2));

            for (ZipEntry var5 = var4.getNextEntry(); var5 != null; var5 = var4.getNextEntry()) {
                String var7 = var5.getName();
                File var8 = new File(var3, var7);
                if (!var5.isDirectory()) {
                    try {
                        var8.createNewFile();
                        FileOutputStream var9 = new FileOutputStream(var8);

                        while (true) {
                            int var10 = var4.read(var6, 0, 8192);
                            if (var10 <= -1) {
                                var9.close();
                                break;
                            }

                            var9.write(var6, 0, var10);
                        }
                    } catch (FileNotFoundException var11) {
                        var11.printStackTrace();
                    } catch (IOException var12) {
                        var12.printStackTrace();
                    }
                } else {
                    var8.mkdirs();
                }

                var4.closeEntry();
            }
        } catch (FileNotFoundException var13) {
            this.mapName = null;
        } catch (IOException var14) {
            this.mapName = null;
        }

        var2.delete();
        this.downloadingMapElement.setAsDownloaded();
        this.downloading = false;
        this.downloadingMap = false;
    }

    private void startMap() {
        String var1 = "";
        File var2 = Minecraft.getGameDirectory();
        File var3 = new File(var2, "saves");
        int var4 = 1;

        File var5;
        do {
            var1 = String.format("%s - Save %d", this.mapName, var4);
            var5 = new File(var3, var1);
            ++var4;
        } while (var5.exists());

        ((ExMinecraft) this.client).saveMapUsed(var1, this.mapName);
        this.client.interactionManager = new SingleplayerInteractionManager(this.client);
        ((ExMinecraft) this.client).startWorld(var1, var1, 0L, this.mapName);
    }

    private boolean downloadFile(String var1, String var2) {
        try {
            URL var3 = new URL(var1);
            URLConnection var4 = var3.openConnection();
            var4.connect();
            BufferedInputStream var5 = null;
            BufferedOutputStream var6 = null;
            File var7 = new File(var2);
            var7.mkdirs();
            if (var7.exists()) {
                var7.delete();
            }

            var7.createNewFile();
            FileOutputStream var8 = new FileOutputStream(var7);
            var5 = new BufferedInputStream(var4.getInputStream());
            var6 = new BufferedOutputStream(var8);
            this.downloadedAmount = 0;
            this.downloadSize = var4.getContentLength();
            byte[] var9 = new byte[65536];

            while (true) {
                int var10 = var5.read(var9, 0, 65536);
                if (var10 == -1) {
                    var6.close();
                    return true;
                }

                var6.write(var9, 0, var10);
                this.downloadedAmount += var10;
            }
        } catch (Exception var11) {
            var11.printStackTrace();
            return false;
        }
    }

    public void drawBackground(int var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        var5.start();
        var5.color(4210752, var4);
        var5.vertex(0.0D, var2, 0.0D, 0.0D, (float) var2 / var6);
        var5.vertex(this.width, var2, 0.0D, (float) this.width / var6, (float) var2 / var6);
        var5.color(4210752, var3);
        var5.vertex(this.width, var1, 0.0D, (float) this.width / var6, (float) var1 / var6);
        var5.vertex(0.0D, var1, 0.0D, 0.0D, (float) var1 / var6);
        var5.tessellate();
    }
}
