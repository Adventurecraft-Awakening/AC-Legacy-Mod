package dev.adventurecraft.awakening.earlyrisers;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.ARBOcclusionQuery;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
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
        // The original game still links LWJGL 2 buffer overloads. Keep these
        // routes until the rendering migration replaces the OpenGL entry points.
        var paths = new LinkedHashSet<>(FabricLauncherBase.getLauncher().getClassPath());
        for (var mod : loader.getAllMods()) paths.addAll(mod.getRootPaths());
        for (Path path : paths) registerLegacyOpenGl(path);
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

    private static final Set<String> LEGACY_OPENGL = Set.of(
        "glFog(ILjava/nio/FloatBuffer;)V",
        "glGetFloat(ILjava/nio/FloatBuffer;)V",
        "glLight(IILjava/nio/FloatBuffer;)V",
        "glLightModel(ILjava/nio/FloatBuffer;)V",
        "glColorPointer(IZILjava/nio/ByteBuffer;)V",
        "glNormalPointer(ILjava/nio/ByteBuffer;)V",
        "glTexCoordPointer(IILjava/nio/FloatBuffer;)V",
        "glVertexPointer(IILjava/nio/FloatBuffer;)V",
        "glGetQueryObjectuARB(IILjava/nio/IntBuffer;)V");
    private static final Set<String> OPENGL_TARGETS = new java.util.HashSet<>();

    private static void registerLegacyOpenGl(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Path game = path.resolve("net/minecraft");
                if (!Files.isDirectory(game)) return;
                try (var files = Files.walk(game)) {
                    for (Path file : files.filter(file -> file.toString().endsWith(".class")).toList())
                        registerLegacyOpenGl(Files.readAllBytes(file));
                }
            } else if (path.toString().endsWith(".jar")) {
                try (var jar = new JarFile(path.toFile())) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        if (!entry.getName().startsWith("net/minecraft/") || !entry.getName().endsWith(".class")) continue;
                        try (var input = jar.getInputStream(entry)) { registerLegacyOpenGl(input.readAllBytes()); }
                    }
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot register LWJGL compatibility in " + path, failure);
        }
    }

    private static void registerLegacyOpenGl(byte[] bytes) {
        var node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        boolean needed = node.methods.stream().anyMatch(method -> {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && legacyOpenGlCall(call)) return true;
            }
            return false;
        });
        if (needed && OPENGL_TARGETS.add(node.name)) {
            ClassTinkerers.addTransformation(node.name, target -> {
                for (var method : target.methods) for (var instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call && legacyOpenGlCall(call)) {
                        call.owner = DESKTOP_BOOTSTRAP;
                        call.itf = false;
                    }
                }
            });
        }
    }

    private static boolean legacyOpenGlCall(MethodInsnNode call) {
        return call.getOpcode() == Opcodes.INVOKESTATIC && call.owner.startsWith("org/lwjgl/opengl/")
            && LEGACY_OPENGL.contains(call.name + call.desc);
    }

    public static void glFog(int parameter, FloatBuffer values) { GL11.glFogfv(parameter, values); }
    public static void glGetFloat(int parameter, FloatBuffer values) { GL11.glGetFloatv(parameter, values); }
    public static void glLight(int light, int parameter, FloatBuffer values) { GL11.glLightfv(light, parameter, values); }
    public static void glLightModel(int parameter, FloatBuffer values) { GL11.glLightModelfv(parameter, values); }
    public static void glColorPointer(int size, boolean unsigned, int stride, ByteBuffer values) {
        GL11.glColorPointer(size, unsigned ? GL11.GL_UNSIGNED_BYTE : GL11.GL_BYTE, stride, values);
    }
    public static void glNormalPointer(int stride, ByteBuffer values) { GL11.glNormalPointer(GL11.GL_BYTE, stride, values); }
    public static void glTexCoordPointer(int size, int stride, FloatBuffer values) { GL11.glTexCoordPointer(size, GL11.GL_FLOAT, stride, values); }
    public static void glVertexPointer(int size, int stride, FloatBuffer values) { GL11.glVertexPointer(size, GL11.GL_FLOAT, stride, values); }
    public static void glGetQueryObjectuARB(int query, int parameter, IntBuffer values) {
        ARBOcclusionQuery.glGetQueryObjectuivARB(query, parameter, values);
    }

}
