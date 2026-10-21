package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.state.ColorMaterial;
import dev.adventurecraft.awakening.client.render.state.CompareOp;
import dev.adventurecraft.awakening.client.render.state.FogDistance;
import dev.adventurecraft.awakening.client.render.state.FogMode;

/** Stable shader codes shared with state_modes.glsl, independent of Java enum order. */
final class VulkanStateCodes {
    static final int COMPARE_NEVER = 0;
    static final int COMPARE_LESS = 1;
    static final int COMPARE_EQUAL = 2;
    static final int COMPARE_LESS_EQUAL = 3;
    static final int COMPARE_GREATER = 4;
    static final int COMPARE_NOT_EQUAL = 5;
    static final int COMPARE_GREATER_EQUAL = 6;
    static final int COMPARE_ALWAYS = 7;

    static final int FOG_NONE = 0;
    static final int FOG_LINEAR = 1;
    static final int FOG_EXPONENTIAL = 2;
    static final int FOG_EXPONENTIAL_SQUARED = 3;

    static final int FOG_DISTANCE_EYE_PLANE = 0;
    static final int FOG_DISTANCE_VERTEX = 1;
    static final int FOG_DISTANCE_FRAGMENT = 2;

    static final int MATERIAL_NONE = 0;
    static final int MATERIAL_AMBIENT = 1;
    static final int MATERIAL_DIFFUSE = 2;
    static final int MATERIAL_AMBIENT_DIFFUSE = 3;
    static final int MATERIAL_EMISSION = 4;

    private VulkanStateCodes() { }

    static int comparison(CompareOp value) {
        return switch (value) {
            case NEVER -> COMPARE_NEVER;
            case LESS -> COMPARE_LESS;
            case EQUAL -> COMPARE_EQUAL;
            case LESS_OR_EQUAL -> COMPARE_LESS_EQUAL;
            case GREATER -> COMPARE_GREATER;
            case NOT_EQUAL -> COMPARE_NOT_EQUAL;
            case GREATER_OR_EQUAL -> COMPARE_GREATER_EQUAL;
            case ALWAYS -> COMPARE_ALWAYS;
        };
    }

    static int fogMode(FogMode value) {
        return switch (value) {
            case NONE -> FOG_NONE;
            case LINEAR -> FOG_LINEAR;
            case EXPONENTIAL -> FOG_EXPONENTIAL;
            case EXPONENTIAL_SQUARED -> FOG_EXPONENTIAL_SQUARED;
        };
    }

    static int fogDistance(FogDistance value) {
        return switch (value) {
            case EYE_PLANE -> FOG_DISTANCE_EYE_PLANE;
            case RADIAL_VERTEX -> FOG_DISTANCE_VERTEX;
            case RADIAL_FRAGMENT -> FOG_DISTANCE_FRAGMENT;
        };
    }

    static int colorMaterial(ColorMaterial value) {
        return switch (value) {
            case NONE -> MATERIAL_NONE;
            case AMBIENT -> MATERIAL_AMBIENT;
            case DIFFUSE -> MATERIAL_DIFFUSE;
            case AMBIENT_AND_DIFFUSE -> MATERIAL_AMBIENT_DIFFUSE;
            case EMISSION -> MATERIAL_EMISSION;
        };
    }
}
