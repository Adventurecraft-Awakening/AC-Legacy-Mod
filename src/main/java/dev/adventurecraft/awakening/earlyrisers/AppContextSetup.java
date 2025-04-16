package dev.adventurecraft.awakening.earlyrisers;

import javax.imageio.ImageIO;

public class AppContextSetup implements Runnable {
    
    @Override
    public void run() {
        ImageIO.setUseCache(false);
    }
}
