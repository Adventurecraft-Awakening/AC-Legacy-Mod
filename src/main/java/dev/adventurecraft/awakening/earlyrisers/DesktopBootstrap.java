package dev.adventurecraft.awakening.earlyrisers;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/** Keeps the desktop client bootstrap independent of Fabric's legacy applet launcher. */
public final class DesktopBootstrap implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger("Adventurecraft/DesktopBootstrap");
    private static final String APPLET_MAIN = "net/fabricmc/loader/impl/game/minecraft/applet/AppletMain";
    private static final String DESKTOP_BOOTSTRAP = "dev/adventurecraft/awakening/earlyrisers/DesktopBootstrap";
    private static final String GAME_DIRECTORY = "(Ljava/io/File;)Ljava/io/File;";

    @Override
    public void run() {
        var loader = FabricLoader.getInstance();
        if (loader.getEnvironmentType() != EnvType.CLIENT) return;
        String client = loader.getMappingResolver().mapClassName("intermediary", "net.minecraft.client.Minecraft");
        ClassTinkerers.addTransformation(client, DesktopBootstrap::routeGameDirectory);
    }

    private static void routeGameDirectory(ClassNode client) {
        int replaced = 0;
        for (var method : client.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC &&
                    call.owner.equals(APPLET_MAIN) && call.name.equals("hookGameDir") && call.desc.equals(GAME_DIRECTORY)) {
                    // Keep Fabric's following Hooks.startClient call: it initializes the loader and mod entrypoints.
                    call.owner = DESKTOP_BOOTSTRAP;
                    call.name = "gameDirectory";
                    replaced++;
                }
            }
        }
        LOGGER.info("Routed {} legacy applet game-directory hook(s) to the desktop launcher", replaced);
    }

    public static File gameDirectory(File directory) {
        // Minecraft.main sets workDir from --gameDir before constructing ACMainThread.
        // The loader's original hook only overrides this for its applet launcher, which we do not use.
        return directory != null ? directory : FabricLoader.getInstance().getGameDir().toFile();
    }
}
