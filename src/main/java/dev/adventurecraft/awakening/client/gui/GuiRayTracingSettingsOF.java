package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import dev.adventurecraft.awakening.client.render.NativeRenderingOptions;
import dev.adventurecraft.awakening.client.render.NativeRenderingOptions.Group;
import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.I18n;

/** Selects supported saved modes and explains the active renderer state. */
public final class GuiRayTracingSettingsOF extends Screen implements OptionTooltipProvider {
    private final Screen previous;
    private final Options options;
    private final Map<Option, String> unavailable = new IdentityHashMap<>();
    private int lastMouseX;
    private int lastMouseY;
    private long mouseStillTime;

    public GuiRayTracingSettingsOF(Screen previous, Options options) {
        this.previous = previous;
        this.options = options;
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.RAY_TRACING, OptionOF.INDIRECT_LIGHTING,
            OptionOF.PARALLAX_MAPPING, OptionOF.RAY_RECONSTRUCTION,
            OptionOF.TEMPORAL_AA, OptionOF.UPSCALER,
            OptionOF.RENDER_SCALE, OptionOF.FRAME_GENERATION};
    }

    @Override
    public void init() {
        Option[] entries = this.getOptions();
        for (int index = 0; index < entries.length; index++) {
            Option option = entries[index];
            int x = this.width / 2 - 155 + index % 2 * 160;
            int y = this.height / 6 + 4 + 24 * (index / 2) + (index >= 4 ? 18 : 0);
            this.buttons.add(new OptionButton(option.getId(), x, y, option, this.options.getMessage(option)));
        }
        this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168,
            I18n.getInstance().get("gui.done")));
        this.refreshCapabilities();
    }

    private void refreshCapabilities() {
        I18n translations = I18n.getInstance();
        ExGameOptions preferences = (ExGameOptions) this.options;
        this.unavailable.clear();
        for (var element : this.buttons) {
            if (!(element instanceof OptionButton button)) continue;
            Option option = button.getOption();
            button.active = true;
            button.message = this.options.getMessage(option);
            Group group = nativeGroup(option);
            if (group != null) {
                int requested = requestedMode(preferences, group);
                int effective = NativeRenderingOptions.effective(group);
                // Menus have no world resolve: show a usable saved selection so
                // clicking a mode has visible feedback before entering a map.
                int displayed = NativeRenderingOptions.status(group, requested).available() ? requested : effective;
                button.message = translations.get(option.getCaptionId()) + ": " + modeLabel(group, displayed, translations);
                button.active = requested != 0 || NativeRenderingOptions.status(group, 1).available()
                    || NativeRenderingOptions.status(group, 2).available();
            } else if (option == OptionOF.RAY_TRACING && !RenderSystem.rayTracingAvailable()) {
                disable(button, translations.get("options.ac.rayTracing.unavailable"), "");
            } else if ((option == OptionOF.INDIRECT_LIGHTING || option == OptionOF.PARALLAX_MAPPING)
                && !RenderSystem.rayTracingEnabled()) {
                disable(button, translations.get("options.ac.inactive"), translations.get("options.ac.requiresRayTracing"));
            } else if (option == OptionOF.INDIRECT_LIGHTING
                && (!RenderSystem.sharcAvailable() || !RenderSystem.sharcUnavailableReason().isEmpty())) {
                disable(button, translations.get("options.ac.indirectLighting.unavailable"), RenderSystem.sharcUnavailableReason());
            } else if (option == OptionOF.TEMPORAL_AA && !RenderSystem.temporalAaAvailable()) {
                disable(button, translations.get("options.ac.temporalAa.unavailable"), RenderSystem.temporalAaUnavailableReason());
            } else if (option == OptionOF.RENDER_SCALE && !RenderSystem.renderScaleAvailable()) {
                disable(button, translations.get("options.ac.renderScale.unavailable"), RenderSystem.renderScaleUnavailableReason());
            } else if (option == OptionOF.PARALLAX_MAPPING && !RenderSystem.parallaxMappingAvailable()) {
                disable(button, translations.get("options.ac.parallaxMapping.unavailable"), translations.get("options.ac.requiresRayTracing"));
            }
        }
    }

    private void disable(OptionButton button, String value, String reason) {
        Option option = button.getOption();
        button.active = false;
        button.message = I18n.getInstance().get(option.getCaptionId()) + ": " + value;
        if (reason != null && !reason.isEmpty()) this.unavailable.put(option, reason);
    }

    @Override
    protected void buttonClicked(Button button) {
        if (!button.active) return;
        if (button instanceof OptionButton optionButton) {
            this.options.toggle(optionButton.getOption(), 1);
            this.refreshCapabilities();
        } else if (button.id == 200) {
            this.closePage();
        }
    }

    @Override
    protected void keyPressed(char character, int key) {
        if (key == 1) this.closePage();
        else super.keyPressed(character, key);
    }

    private void closePage() {
        this.options.save();
        this.minecraft.setScreen(this.previous);
    }

    @Override
    public List<String> getTooltipLines(GuiComponent element) {
        if (!(element instanceof OptionButton button)) return null;
        Option option = button.getOption();
        I18n translations = I18n.getInstance();
        if (option == OptionOF.RAY_TRACING && !RenderSystem.rayTracingAvailable()) {
            return OptionTooltipProvider.parseOptionTooltip(translations.get("options.ac.rayTracing.unavailable.tooltip"));
        }
        String reason = this.unavailable.get(option);
        if (reason != null) return List.of(reason);
        List<String> defaults = OptionTooltipProvider.super.getTooltipLines(element);
        Group group = nativeGroup(option);
        if (group == null) return defaults;
        var lines = new ArrayList<String>();
        if (defaults != null) lines.addAll(defaults);
        int requested = requestedMode((ExGameOptions) this.options, group);
        lines.add(translations.get("options.ac.effective") + ": "
            + modeLabel(group, NativeRenderingOptions.effective(group), translations));
        if (requested != NativeRenderingOptions.effective(group)) {
            lines.add(translations.get("options.ac.requested") + ": " + modeLabel(group, requested, translations));
        }
        for (int mode = 1; mode <= 2; mode++) {
            var status = NativeRenderingOptions.status(group, mode);
            String detail = status.available() ? translations.get("options.ac.available") : status.reason();
            lines.add(modeLabel(group, mode, translations) + ": " + detail);
        }
        return lines;
    }

    private static Group nativeGroup(Option option) {
        if (option == OptionOF.RAY_RECONSTRUCTION) return Group.RECONSTRUCTION;
        if (option == OptionOF.UPSCALER) return Group.UPSCALER;
        if (option == OptionOF.FRAME_GENERATION) return Group.FRAME_GENERATION;
        return null;
    }

    private static int requestedMode(ExGameOptions preferences, Group group) {
        return switch (group) {
            case RECONSTRUCTION -> preferences.rayReconstruction();
            case UPSCALER -> preferences.upscaler();
            case FRAME_GENERATION -> preferences.frameGeneration();
        };
    }

    private static String modeLabel(Group group, int mode, I18n translations) {
        String key = switch (group) {
            case RECONSTRUCTION -> mode == 1 ? "options.ac.rayReconstruction.dlss"
                : mode == 2 ? "options.ac.rayReconstruction.nrd" : "options.ac.rayReconstruction.builtin";
            case UPSCALER -> mode == 1 ? "options.ac.upscaler.dlss"
                : mode == 2 ? "options.ac.upscaler.fsr" : "options.ac.upscaler.cubic";
            case FRAME_GENERATION -> mode == 1 ? "options.ac.frameGeneration.dlss"
                : mode == 2 ? "options.ac.frameGeneration.fsr" : "options.ac.frameGeneration.off";
        };
        return translations.get(key);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.refreshCapabilities();
        I18n translations = I18n.getInstance();
        this.renderBackground();
        this.drawCenteredString(this.font, translations.get("options.ac.rayTracingSettings.title"), this.width / 2, 20, 0xFFFFFF);
        this.drawCenteredString(this.font, translations.get("options.ac.rayTracingSettings.lighting"), this.width / 2,
            this.height / 6 - 6, 0xC0C0C0);
        this.drawCenteredString(this.font, translations.get("options.ac.rayTracingSettings.image"), this.width / 2,
            this.height / 6 + 60, 0xC0C0C0);
        this.drawCenteredString(this.font, translations.get("options.ac.rayTracingSettings.classic"), this.width / 2,
            this.height / 6 + 144, 0xA0A0A0);
        super.render(mouseX, mouseY, delta);
        if (Math.abs(mouseX - this.lastMouseX) > 5 || Math.abs(mouseY - this.lastMouseY) > 5) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseStillTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() >= this.mouseStillTime + 700) {
            this.renderCapabilityTooltip(mouseX, mouseY);
        }
    }

    private void renderCapabilityTooltip(int mouseX, int mouseY) {
        Button selected = GuiBaseSettingsOF.getSelectedButton(this.buttons, mouseX, mouseY);
        if (selected == null) return;
        List<String> source = this.getTooltipLines(selected);
        if (source == null) return;
        int boxWidth = Math.min(300, this.width - 16);
        var lines = new ArrayList<String>();
        for (String message : source) {
            String line = "";
            for (String word : message.split(" ")) {
                String next = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && this.font.width(next) > boxWidth - 10) {
                    lines.add(line);
                    line = word;
                } else line = next;
            }
            if (!line.isEmpty()) lines.add(line);
        }
        int count = Math.min(lines.size(), Math.max(1, (this.height - 22) / 11));
        int boxHeight = 10 + count * 11;
        int x = (this.width - boxWidth) / 2;
        int y = mouseY < this.height / 2 ? Math.min(this.height - boxHeight - 6, mouseY + 12)
            : Math.max(6, mouseY - boxHeight - 12);
        this.fillGradient(x, y, x + boxWidth, y + boxHeight, 0xE0101010, 0xE0101010);
        for (int index = 0; index < count; index++) {
            String text = index == count - 1 && lines.size() > count ? "..." : lines.get(index);
            this.font.drawShadow(text, x + 5, y + 5 + index * 11, 0xDDDDDD);
        }
    }
}
