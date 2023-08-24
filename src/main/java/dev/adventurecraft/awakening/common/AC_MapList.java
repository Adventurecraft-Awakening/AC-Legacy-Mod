package dev.adventurecraft.awakening.common;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class AC_MapList {
	private List availableMaps = new ArrayList();
	private File mapDir;

    public AC_MapList() {
        this.mapDir = ACMainThread.getMapsDirectory();
        if (!this.mapDir.exists()) {
            this.mapDir.mkdirs();
        }

        this.findMaps();
    }

	public void findMaps() {
		ArrayList var1 = new ArrayList();
		if(this.mapDir.exists() && this.mapDir.isDirectory()) {
			File[] var2 = this.mapDir.listFiles();
			File[] var3 = var2;
			int var4 = var2.length;

			for(int var5 = 0; var5 < var4; ++var5) {
				File var6 = var3[var5];
				if(var6.isDirectory()) {
					String var7 = var6.getName();
					String var8 = "";
					String var9 = "";
					File var10 = new File(var6, "description.txt");

					try {
						BufferedReader var11 = new BufferedReader(new FileReader(var10));
						var8 = var11.readLine();
						var9 = var11.readLine();
					} catch (FileNotFoundException var16) {
					} catch (IOException var17) {
					}

					File var18 = new File(var6, "thumbnail.png");
					BufferedImage var12 = null;

					try {
						var12 = ImageIO.read(var18);
					} catch (FileNotFoundException var14) {
					} catch (IOException var15) {
					}

					var1.add(new AC_MapInfo(var7, var8, var9, var12));
				}
			}
		}

		this.availableMaps = var1;
	}

	public List availableMaps() {
		return new ArrayList(this.availableMaps);
	}
}
