package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptableObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The map scripting API's names and constructors are registered in one catalog. */
final class ScriptBindings {
    private static final List<Map.Entry<String, Class<?>>> CLASSES = List.of(
        Map.entry("Item", ScriptItem.class), Map.entry("UILabel", ScriptUILabel.class),
        Map.entry("UISprite", ScriptUISprite.class), Map.entry("UIRect", ScriptUIRect.class),
        Map.entry("UIContainer", ScriptUIContainer.class), Map.entry("Model", ScriptModel.class),
        Map.entry("ModelBlockbench", ScriptModelBlockbench.class), Map.entry("Vec3", ScriptVec3.class),
        Map.entry("Vec4", ScriptVec4.class), Map.entry("VecRot", ScriptVecRot.class));

    final ScriptTime time;
    final ScriptKeyboard keyboard;
    private final WorldScriptRuntime runtime;

    ScriptBindings(WorldScriptRuntime runtime, Level world, Minecraft client) {
        this.runtime = runtime;
        this.time = new ScriptTime(world);
        this.keyboard = new ScriptKeyboard(world, client.options, runtime.newScope());
        Map<String, Object> objects = new LinkedHashMap<>();
        objects.put("time", this.time);
        objects.put("world", new ScriptWorld(world));
        objects.put("chat", new ScriptChat());
        objects.put("weather", new ScriptWeather(world));
        objects.put("effect", new ScriptEffect(world, client.levelRenderer));
        objects.put("particle", new ScriptParticle(client.levelRenderer));
        objects.put("sound", new ScriptSound(world, client.soundEngine));
        objects.put("ui", new ScriptUI());
        objects.put("screen", ((ExInGameHud) client.gui).getScriptUI());
        objects.put("script", new ScriptScript(world));
        objects.put("keyboard", this.keyboard);
        objects.put("hitEntity", null);
        objects.put("hitBlock", null);
        objects.put("renderer", new ScriptRenderer(client.levelRenderer));
        objects.forEach(runtime::bind);
    }

    void installClasses() {
        this.runtime.withContext(() -> {
            CLASSES.forEach(entry -> ScriptableObject.putProperty(this.runtime.globalScope(), entry.getKey(),
                new NativeJavaClass(this.runtime.globalScope(), entry.getValue())));
            return null;
        });
    }

    void player(LocalPlayer player) { this.runtime.bind("player", new ScriptEntityPlayer(player)); }
}
