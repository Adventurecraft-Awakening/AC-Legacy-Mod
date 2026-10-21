package dev.adventurecraft.awakening.earlyrisers;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

/** Routes the completed game classes, including their injected mixin methods. */
public final class VulkanRenderRouting implements Runnable, IExtension {
    private static final Logger LOGGER = LoggerFactory.getLogger("Adventurecraft/VulkanRouting");
    private static final String RENDER_SYSTEM = "dev/adventurecraft/awakening/client/render/compat/LegacyGlAdapter";
    private static final String MOD_PREFIX = "dev/adventurecraft/awakening/";
    private static final String[] ROOTS = {"net/minecraft/", MOD_PREFIX, "org/lwjgl/util/glu/"};
    private final Map<String, LegacyGlAbi.Entry> methods = new HashMap<>();
    private final Map<String, LegacyGlAbi.Entry> adapterMethods = new HashMap<>();
    private final Set<String> audit = new java.util.TreeSet<>();
    private final Path auditOutput = auditPath();
    private final Set<String> targets = new HashSet<>();
    private final Set<String> reported = new HashSet<>();

    @Override
    public void run() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }

        // Check only explicitly permitted routes without initializing the graphics device.
        try (var source = VulkanRenderRouting.class.getClassLoader().getResourceAsStream(RENDER_SYSTEM + ".class")) {
            if (source == null) throw new IllegalStateException("Missing Vulkan compatibility renderer");
            this.methods.putAll(LegacyGlAbi.load());
            var node = new ClassNode();
            new ClassReader(source).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            Set<String> publicMethods = new HashSet<>();
            for (var method : node.methods) {
                if ((method.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) {
                    publicMethods.add(method.name + method.desc);
                }
            }
            for (var entry : this.methods.values()) {
                if (entry.support() == LegacyGlAbi.Support.UNSUPPORTED) continue;
                String target = entry.target() + entry.descriptor();
                if (!publicMethods.contains(target)) throw new IllegalStateException("Missing explicit legacy GL ABI target: " + target);
                this.adapterMethods.put(target, entry);
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read Vulkan renderer ABI contract", failure);
        }

        var paths = new LinkedHashSet<>(FabricLauncherBase.getLauncher().getClassPath());
        for (var mod : FabricLoader.getInstance().getAllMods()) paths.addAll(mod.getRootPaths());
        for (Path path : paths) this.discover(path);
        this.writeAudit();

        // MM's transformations run before mixin injection. Register empty ones to
        // make ordinary mod/GLU classes targets too, then rewrite only in the
        // extension's postApply after @Redirect and @Inject have found GL calls.
        for (String target : this.targets) ClassTinkerers.addTransformation(target, ignored -> { });
        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (!(transformer instanceof IMixinTransformer mixinTransformer)
            || !(mixinTransformer.getExtensions() instanceof Extensions extensions)) {
            throw new IllegalStateException("The Mixin transformer cannot install Vulkan render routing");
        }
        extensions.add(this);
        // Early risers execute during config preparation, after Mixin's first
        // extension selection. Include this newly installed extension now.
        extensions.select(MixinEnvironment.getCurrentEnvironment());
        LOGGER.info("Vulkan rendering routes registered for {} game classes ({} entry points)",
            this.targets.size(), this.methods.size());
    }

    private void discover(Path path) {
        try {
            if (Files.isDirectory(path)) {
                for (String root : ROOTS) {
                    Path directory = path.resolve(root);
                    if (!Files.isDirectory(directory)) continue;
                    try (var files = Files.walk(directory)) {
                        for (Path file : files.filter(Files::isRegularFile).toList()) {
                            String name = path.relativize(file).toString().replace('\\', '/');
                            if (isCandidate(name)) this.discoverClass(name, Files.readAllBytes(file));
                        }
                    }
                }
            } else if (path.toString().endsWith(".jar")) {
                try (var jar = new JarFile(path.toFile())) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        if (!isCandidate(entry.getName())) continue;
                        try (var source = jar.getInputStream(entry)) {
                            this.discoverClass(entry.getName(), source.readAllBytes());
                        }
                    }
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot register Vulkan routing in " + path, failure);
        }
    }

    private static boolean isCandidate(String name) {
        return name.endsWith(".class") && isRoutedClass(name.substring(0, name.length() - ".class".length()));
    }

    private static boolean isRoutedClass(String name) {
        if (name.startsWith(MOD_PREFIX + "mixin/") || name.startsWith(MOD_PREFIX + "earlyrisers/")
            || name.startsWith(MOD_PREFIX + "client/render/vulkan/")
            || name.startsWith(MOD_PREFIX + "client/render/compat/")
            || name.equals(MOD_PREFIX + "client/render/compat/LegacyGlAdapter")) return false;
        for (String root : ROOTS) {
            if (name.startsWith(root)) return true;
        }
        return false;
    }

    private void discoverClass(String name, byte[] bytes) {
        boolean[] callsOpenGL = {false};
        String caller = name.substring(0, name.length() - ".class".length());
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String method, String methodDescriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String calledMethod, String descriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESTATIC && owner.startsWith("org/lwjgl/opengl/") && calledMethod.startsWith("gl")) {
                            callsOpenGL[0] = true;
                            recordAudit("original", caller + "#" + method + methodDescriptor, owner, calledMethod + descriptor);
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        if (callsOpenGL[0]) this.targets.add(caller);
    }

    private static Path auditPath() {
        String property = System.getProperty("ac.vulkan.abiAudit");
        return property == null || property.equals("false") ? null
            : property.equals("true") ? Path.of("build", "vulkan-abi.tsv") : Path.of(property);
    }
    private synchronized void recordAudit(String phase, String caller, String owner, String signature) {
        LegacyGlAbi.Entry entry = this.methods.get(signature);
        String status = entry == null ? "unknown" : entry.support().name().toLowerCase(java.util.Locale.ROOT);
        this.audit.add(String.join("\t", phase, status, owner + "." + signature,
            entry == null ? "-" : entry.target(), caller));
    }
    private synchronized void writeAudit() {
        if (this.auditOutput == null) return;
        try {
            Path parent = this.auditOutput.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            var rows = new java.util.ArrayList<String>();
            rows.add("phase\tstatus\tsignature\tadapter_target\tcaller");
            rows.addAll(this.audit);
            Files.write(this.auditOutput, rows, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot export Vulkan ABI audit to " + this.auditOutput, failure);
        }
    }

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) { }

    @Override
    public void postApply(ITargetClassContext context) {
        ClassNode node = context.getClassNode();
        if (!isRoutedClass(node.name)) return;
        var bridges = new java.util.ArrayList<MethodNode>();
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESTATIC) continue;
                String caller = node.name + "#" + method.name + method.desc;
                if (call.owner.equals(RENDER_SYSTEM)) {
                    var entry = this.adapterMethods.get(call.name + call.desc);
                    if (entry != null) this.recordAudit("post-adapter", caller, call.owner, entry.signature());
                    continue;
                }
                if (!call.owner.startsWith("org/lwjgl/opengl/") || !call.name.startsWith("gl")) continue;
                String signature = call.name + call.desc;
                this.recordAudit("post-mixin", caller, call.owner, signature);
                LegacyGlAbi.Entry entry = this.methods.get(signature);
                if (entry != null && entry.support() != LegacyGlAbi.Support.UNSUPPORTED) {
                    call.owner = RENDER_SYSTEM;
                    call.name = entry.target();
                    call.itf = false;
                } else {
                    if (entry == null && Boolean.getBoolean("ac.vulkan.strictAbi")) {
                        this.writeAudit();
                        throw new IllegalStateException("Unlisted OpenGL ABI signature " + call.owner + "." + signature + " in " + caller);
                    }
                    // Unported optional GL extensions must never reach a native
                    // OpenGL entry point on a GLFW_NO_API window.
                    String original = call.owner + "." + signature;
                    String name = "ac$vulkanUnsupported$" + bridges.size();
                    bridges.add(unsupported(name, call.desc, original));
                    call.owner = node.name;
                    call.name = name;
                    call.itf = (node.access & Opcodes.ACC_INTERFACE) != 0;
                    if (this.reported.add(original)) LOGGER.warn("OpenGL entry point unavailable in Vulkan ({}): {}",
                        entry == null ? "unlisted ABI" : "explicitly unsupported", original);
                }
            }
        }
        node.methods.addAll(bridges);
        this.writeAudit();
    }

    private static MethodNode unsupported(String name, String descriptor, String original) {
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
            name, descriptor, null, null);
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/UnsupportedOperationException"));
        method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new LdcInsnNode("Vulkan does not support this OpenGL extension: " + original));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/UnsupportedOperationException",
            "<init>", "(Ljava/lang/String;)V", false));
        method.instructions.add(new InsnNode(Opcodes.ATHROW));
        method.maxStack = 3;
        for (Type argument : Type.getArgumentTypes(descriptor)) method.maxLocals += argument.getSize();
        return method;
    }

    @Override
    public void export(MixinEnvironment environment, String name, boolean force, ClassNode node) { }
}
