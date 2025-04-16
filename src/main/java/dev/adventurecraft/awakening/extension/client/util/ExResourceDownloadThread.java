package dev.adventurecraft.awakening.extension.client.util;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public interface ExResourceDownloadThread {

    static void loadSoundsFromResources(Minecraft client, Class<?> rootClass, String resourcePath) {
        String resPath = resourcePath.replaceFirst("/", "");
        ACMod.LOGGER.info(
            "Loading sounds from path \"{}\" in \"{}\".",
            resPath, rootClass.getName());

        URI rootUri;
        try {
            rootUri = rootClass.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException ex) {
            ACMod.LOGGER.error("Failed to get class location.", ex);
            return;
        }

        Path dirPath = Path.of(rootUri).resolve(resPath);
        if (Files.isDirectory(dirPath)) {
            try {
                loadSounds(client, dirPath);
            } catch (IOException ex) {
                ACMod.LOGGER.error("Failed to load sounds from directory.", ex);
            }
            return;
        }

        try {
            URI jarUri = URI.create(String.format("jar:%s!/", rootUri));
            FileSystem jarFs = FileSystems.getFileSystem(jarUri);
            Path jarPath = jarFs.getPath(resPath);
            loadSounds(client, jarPath);
        } catch (IOException ex) {
            ACMod.LOGGER.error("Failed to load sounds from JAR.", ex);
        } catch (ProviderNotFoundException | FileSystemNotFoundException ex) {
            RuntimeException logEx = ex;
            if (logEx instanceof FileSystemNotFoundException notFound && notFound.getMessage() == null) {
                logEx = null;
            }
            ACMod.LOGGER.warn("Not loading sounds from JAR because that filesystem is not available.", logEx);
        }
    }

    private static void loadSounds(Minecraft client, Path rootPath) throws IOException {
        var count = new AtomicInteger();
        var totalCount = new AtomicInteger();
        Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (!path.getFileName().toString().toLowerCase().endsWith(".ogg")) {
                    return FileVisitResult.CONTINUE;
                }
                Path relativePath = rootPath.relativize(path);
                String separator = rootPath.getFileSystem().getSeparator();
                String namePath = relativePath.toString().replace(separator, "/");

                totalCount.incrementAndGet();
                try {
                    ((ExMinecraft) client).loadSoundFromDir(namePath, path.toUri().toURL());
                    count.incrementAndGet();
                    ACMod.LOGGER.debug("Loaded sound \"{}\" (\"{}\").", namePath, path);
                } catch (IOException ex) {
                    ACMod.LOGGER.warn("Failed to load sound \"{}\" (\"{}\").", namePath, path, ex);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        ACMod.LOGGER.info("Loaded {} out of {} sounds from \"{}\".", count, totalCount, rootPath);
    }
}
