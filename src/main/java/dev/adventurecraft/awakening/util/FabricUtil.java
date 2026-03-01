package dev.adventurecraft.awakening.util;

import net.fabricmc.loader.api.metadata.CustomValue;
import org.spongepowered.include.com.google.gson.*;

public final class FabricUtil {

    public static JsonElement toJson(CustomValue value) {
        return switch (value.getType()) {
            case OBJECT -> {
                var target = new JsonObject();
                for (var entry : value.getAsObject()) {
                    target.add(entry.getKey(), toJson(entry.getValue()));
                }
                yield target;
            }
            case ARRAY -> {
                var target = new JsonArray();
                for (var item : value.getAsArray()) {
                    target.add(toJson(item));
                }
                yield target;
            }
            case STRING -> new JsonPrimitive(value.getAsString());
            case NUMBER -> new JsonPrimitive(value.getAsNumber());
            case BOOLEAN -> new JsonPrimitive(value.getAsBoolean());
            case NULL -> JsonNull.INSTANCE;
        };
    }
}
