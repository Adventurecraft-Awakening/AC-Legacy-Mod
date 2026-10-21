package dev.adventurecraft.awakening.client.render.material;

import org.spongepowered.include.com.google.gson.JsonArray;
import org.spongepowered.include.com.google.gson.JsonElement;
import org.spongepowered.include.com.google.gson.JsonObject;
import org.spongepowered.include.com.google.gson.JsonPrimitive;

import java.util.Map;

/**
 * Sparse material description. A {@code null} field means "inherit", which lets a
 * map manifest override only the values it cares about while the built-in
 * registry defaults fill the rest.
 */
public final class MaterialProfile {

    public static final int FLAG_THIN = 1;
    public static final int FLAG_DECORATIVE = 2;
    public static final int FLAG_WATER = 4;
    public static final int FLAG_TRANSMISSIVE = 8;

    public String material;
    public Integer emission;
    public Integer flags;
    public Integer lightColor;
    public Float lightRadius;
    public Float lightIntensity;
    public Float transmission;
    public Float ior;
    public Float absorptionR;
    public Float absorptionG;
    public Float absorptionB;
    public Float smoothness;
    public Float f0;
    public Boolean metallic;
    public Float porosity;
    public Float sss;
    public Boolean parallax;
    public Integer gameplayLight;

    public MaterialProfile() { }

    public MaterialProfile(MaterialProfile other) {
        this.copyFrom(other);
    }

    public static MaterialProfile parse(JsonObject object) {
        MaterialProfile profile = new MaterialProfile();
        if (object == null) return profile;

        profile.material = string(object, "material");
        profile.emission = integer(object, "emission");
        profile.flags = integer(object, "flags");
        profile.lightColor = parseColor(string(object, "color"));
        profile.lightRadius = number(object, "radius");
        profile.lightIntensity = number(object, "intensity");
        profile.transmission = number(object, "transmission");
        profile.ior = number(object, "ior");
        profile.smoothness = number(object, "smoothness");
        profile.f0 = number(object, "f0");
        profile.metallic = bool(object, "metallic");
        profile.porosity = number(object, "porosity");
        profile.sss = number(object, "sss");
        profile.parallax = bool(object, "parallax");
        profile.gameplayLight = integer(object, "gameplayLight");

        JsonElement light = field(object, "light");
        if (light != null && light.isJsonObject()) {
            JsonObject lightObject = light.getAsJsonObject();
            Integer color = parseColor(string(lightObject, "color"));
            if (color != null) profile.lightColor = color;
            Float radius = number(lightObject, "radius");
            if (radius != null) profile.lightRadius = radius;
            Float intensity = number(lightObject, "intensity");
            if (intensity != null) profile.lightIntensity = intensity;
        }

        JsonElement absorption = field(object, "absorption");
        if (absorption != null && absorption.isJsonArray()) {
            int index = 0;
            for (JsonElement component : absorption.getAsJsonArray()) {
                float value = (float) component.getAsDouble();
                if (index == 0) profile.absorptionR = value;
                else if (index == 1) profile.absorptionG = value;
                else if (index == 2) profile.absorptionB = value;
                if (++index >= 3) break;
            }
        }

        if (boolValue(object, "thin")) profile.addFlag(FLAG_THIN);
        if (boolValue(object, "decorative")) profile.addFlag(FLAG_DECORATIVE);
        if (boolValue(object, "water")) profile.addFlag(FLAG_WATER);
        if (profile.transmission != null && profile.transmission > 0.0F) {
            profile.addFlag(FLAG_TRANSMISSIVE);
        }
        return profile;
    }

    public void addFlag(int flag) {
        this.flags = (this.flags == null ? 0 : this.flags) | flag;
    }

    public boolean hasFlag(int flag) {
        return this.flags != null && (this.flags & flag) != 0;
    }

    public void copyFrom(MaterialProfile other) {
        if (other == null) return;
        if (other.material != null) this.material = other.material;
        if (other.emission != null) this.emission = other.emission;
        if (other.flags != null) this.flags = other.flags;
        if (other.lightColor != null) this.lightColor = other.lightColor;
        if (other.lightRadius != null) this.lightRadius = other.lightRadius;
        if (other.lightIntensity != null) this.lightIntensity = other.lightIntensity;
        if (other.transmission != null) this.transmission = other.transmission;
        if (other.ior != null) this.ior = other.ior;
        if (other.absorptionR != null) this.absorptionR = other.absorptionR;
        if (other.absorptionG != null) this.absorptionG = other.absorptionG;
        if (other.absorptionB != null) this.absorptionB = other.absorptionB;
        if (other.smoothness != null) this.smoothness = other.smoothness;
        if (other.f0 != null) this.f0 = other.f0;
        if (other.metallic != null) this.metallic = other.metallic;
        if (other.porosity != null) this.porosity = other.porosity;
        if (other.sss != null) this.sss = other.sss;
        if (other.parallax != null) this.parallax = other.parallax;
        if (other.gameplayLight != null) this.gameplayLight = other.gameplayLight;
    }

    public MaterialProfile resolve(MaterialProfile defaults, Map<String, MaterialProfile> palette) {
        MaterialProfile result = new MaterialProfile();
        result.copyFrom(defaults);
        if (this.material != null && palette != null) {
            MaterialProfile preset = palette.get(this.material);
            if (preset != null) result.copyFrom(preset);
        }
        result.copyFrom(this);
        return result;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        if (this.material != null) object.add("material", new JsonPrimitive(this.material));
        if (this.emission != null) object.add("emission", new JsonPrimitive(this.emission));
        if (this.flags != null) {
            if ((this.flags & FLAG_THIN) != 0) object.add("thin", new JsonPrimitive(true));
            if ((this.flags & FLAG_DECORATIVE) != 0) object.add("decorative", new JsonPrimitive(true));
            if ((this.flags & FLAG_WATER) != 0) object.add("water", new JsonPrimitive(true));
        }
        if (this.lightColor != null || this.lightRadius != null || this.lightIntensity != null) {
            JsonObject light = new JsonObject();
            if (this.lightColor != null) light.add("color", new JsonPrimitive(formatColor(this.lightColor)));
            if (this.lightRadius != null) light.add("radius", new JsonPrimitive(this.lightRadius));
            if (this.lightIntensity != null) light.add("intensity", new JsonPrimitive(this.lightIntensity));
            object.add("light", light);
        }
        if (this.transmission != null) object.add("transmission", new JsonPrimitive(this.transmission));
        if (this.ior != null) object.add("ior", new JsonPrimitive(this.ior));
        if (this.absorptionR != null || this.absorptionG != null || this.absorptionB != null) {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(this.absorptionR == null ? 0.0F : this.absorptionR));
            array.add(new JsonPrimitive(this.absorptionG == null ? 0.0F : this.absorptionG));
            array.add(new JsonPrimitive(this.absorptionB == null ? 0.0F : this.absorptionB));
            object.add("absorption", array);
        }
        if (this.smoothness != null) object.add("smoothness", new JsonPrimitive(this.smoothness));
        if (this.f0 != null) object.add("f0", new JsonPrimitive(this.f0));
        if (this.metallic != null) object.add("metallic", new JsonPrimitive(this.metallic));
        if (this.porosity != null) object.add("porosity", new JsonPrimitive(this.porosity));
        if (this.sss != null) object.add("sss", new JsonPrimitive(this.sss));
        if (this.parallax != null) object.add("parallax", new JsonPrimitive(this.parallax));
        if (this.gameplayLight != null) object.add("gameplayLight", new JsonPrimitive(this.gameplayLight));
        return object;
    }

    public static Integer parseColor(String text) {
        if (text == null || text.isEmpty()) return null;
        String value = text.startsWith("#") ? text.substring(1) : text;
        try {
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String formatColor(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private static JsonElement field(JsonObject object, String key) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getKey().equals(key)) return entry.getValue();
        }
        return null;
    }

    private static String string(JsonObject object, String key) {
        JsonElement element = field(object, key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static Integer integer(JsonObject object, String key) {
        JsonElement element = field(object, key);
        return element != null && element.isJsonPrimitive() ? element.getAsInt() : null;
    }

    private static Float number(JsonObject object, String key) {
        JsonElement element = field(object, key);
        return element != null && element.isJsonPrimitive() ? (float) element.getAsDouble() : null;
    }

    private static Boolean bool(JsonObject object, String key) {
        JsonElement element = field(object, key);
        return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : null;
    }

    private static boolean boolValue(JsonObject object, String key) {
        Boolean value = bool(object, key);
        return value != null && value;
    }
}
