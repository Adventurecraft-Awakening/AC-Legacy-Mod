package dev.adventurecraft.awakening.tools;

import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Compiles packaged shaders during the build; the game needs no GLSL compiler. */
public final class ShaderCompiler {
    private static final Pattern INCLUDE = Pattern.compile("(?m)^\\s*#include\\s+(?:\"([^\"]+)\"|<([^>]+)>)\\s*$");

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]);
        Path sdk = args.length > 2 ? Path.of(args[2]).toAbsolutePath().normalize() : null;
        Files.createDirectories(output);
        removeOptionalShaders(output);
        long compiler = Shaderc.shaderc_compiler_initialize();
        if (compiler == 0) throw new IllegalStateException("Cannot initialize shader compiler");
        long options = Shaderc.shaderc_compile_options_initialize();
        if (options == 0) {
            Shaderc.shaderc_compiler_release(compiler);
            throw new IllegalStateException("Cannot initialize shader compiler options");
        }
        try {
            Shaderc.shaderc_compile_options_set_optimization_level(options, Shaderc.shaderc_optimization_level_performance);
            try (var files = Files.list(root)) {
                for (Path path : files.sorted().toList()) {
                    String name = path.getFileName().toString();
                    if (name.startsWith("sharc_") && sdk == null) continue;
                    int kind;
                    if (name.endsWith(".vert")) kind = Shaderc.shaderc_glsl_vertex_shader;
                    else if (name.endsWith(".frag")) kind = Shaderc.shaderc_glsl_fragment_shader;
                    else if (name.endsWith(".comp")) kind = Shaderc.shaderc_glsl_compute_shader;
                    else continue;
                    // Ray queries and physical storage buffers require SPIR-V 1.4+.
                    // Keep the ordinary shaders usable by older raster-only devices.
                    Shaderc.shaderc_compile_options_set_target_env(options, Shaderc.shaderc_target_env_vulkan,
                        name.startsWith("terrain_ray.") || name.startsWith("terrain_ray_hdr.") || name.startsWith("terrain_restir.")
                            || name.equals("irradiance.comp") || name.startsWith("restir_") || name.startsWith("sharc_")
                            ? Shaderc.shaderc_env_version_vulkan_1_2 : Shaderc.shaderc_env_version_vulkan_1_0);
                    // Expanded SDK sources exceed LWJGL's small thread-local stack.
                    // Use explicitly freed native strings rather than CharSequence overloads.
                    var source = MemoryUtil.memUTF8(expand(root, sdk, path, new HashSet<>()), false);
                    var sourceName = MemoryUtil.memUTF8(name);
                    var entryPoint = MemoryUtil.memUTF8("main");
                    long result;
                    try {
                        result = Shaderc.shaderc_compile_into_spv(compiler, source, kind, sourceName, entryPoint, options);
                    } finally {
                        MemoryUtil.memFree(entryPoint);
                        MemoryUtil.memFree(sourceName);
                        MemoryUtil.memFree(source);
                    }
                    if (result == 0) throw new IllegalStateException("Shader compiler returned no result for " + name);
                    try {
                        if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
                            throw new IllegalStateException(name + ": " + Shaderc.shaderc_result_get_error_message(result));
                        }
                        var bytes = Shaderc.shaderc_result_get_bytes(result);
                        if (bytes == null) throw new IllegalStateException("Empty compiled shader: " + name);
                        try (var channel = Files.newByteChannel(output.resolve(name + ".spv"),
                            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                            java.nio.file.StandardOpenOption.WRITE)) {
                            while (bytes.hasRemaining()) channel.write(bytes);
                        }
                    } finally {
                        Shaderc.shaderc_result_release(result);
                    }
                }
            }
        } catch (IOException | RuntimeException | Error error) {
            removeOptionalShaders(output);
            throw error;
        } finally {
            Shaderc.shaderc_compile_options_release(options);
            Shaderc.shaderc_compiler_release(compiler);
        }
    }

    private static void removeOptionalShaders(Path output) throws IOException {
        try (var files = Files.list(output)) {
            for (Path file : files.filter(path -> path.getFileName().toString().startsWith("sharc_")
                && path.getFileName().toString().endsWith(".comp.spv")).toList()) Files.delete(file);
        }
    }

    private static String expand(Path root, Path sdk, Path file, Set<Path> visiting) throws IOException {
        file = file.normalize();
        if (!file.startsWith(root) && (sdk == null || !file.startsWith(sdk))) {
            throw new IOException("Shader include escapes source directories: " + file);
        }
        if (!visiting.add(file)) throw new IOException("Recursive shader include: " + file);
        try {
            var matcher = INCLUDE.matcher(Files.readString(file));
            var expanded = new StringBuilder();
            while (matcher.find()) {
                Path include;
                if (matcher.group(2) != null) {
                    if (sdk == null) throw new IOException("Shader requires an external SDK: " + matcher.group(2));
                    include = sdk.resolve(matcher.group(2));
                } else include = file.getParent().resolve(matcher.group(1));
                matcher.appendReplacement(expanded, java.util.regex.Matcher.quoteReplacement(
                    expand(root, sdk, include, visiting)));
            }
            matcher.appendTail(expanded);
            return expanded.toString();
        } finally {
            visiting.remove(file);
        }
    }
}
