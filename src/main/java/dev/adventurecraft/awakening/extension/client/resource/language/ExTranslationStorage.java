package dev.adventurecraft.awakening.extension.client.resource.language;

import java.io.File;

public interface ExTranslationStorage {

    void loadMapTranslation(File var1);

    String getOr(String key, String fallback);
}
