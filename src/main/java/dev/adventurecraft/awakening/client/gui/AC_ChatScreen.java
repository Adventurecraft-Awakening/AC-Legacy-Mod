package dev.adventurecraft.awakening.client.gui;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.adventurecraft.awakening.client.gui.components.AC_EditBox;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.common.ServerCommandSource;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.js.CodeSuggestion;
import dev.adventurecraft.awakening.js.Evaluator;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.util.StringUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class AC_ChatScreen extends Screen {

    public static final String JS_PROMPT = ""; // TODO: change to "$" ???

    protected final LocalPlayer player;

    protected AC_EditBox messageBox;

    private @Nullable CompletableFuture<Suggestions> suggestionFuture;

    public AC_ChatScreen(LocalPlayer player) {
        this.player = player;
    }

    public @Override void init() {
        Keyboard.enableRepeatEvents(true);

        int chatH = 16;
        int barOffset = 32; // TODO: drive value from InGameHud
        var border = new IntBorder(2);

        var rect = new IntRect(0, this.height - chatH - barOffset, this.width, chatH).shrink(border);
        if (this.messageBox == null) {
            this.messageBox = new AC_EditBox(rect, "");
            this.messageBox.setActive(true);

            this.messageBox.setBoxBackColor(Rgba.withAlpha(this.messageBox.getBoxBackColor(), 100));
            this.messageBox.setBoxBorderColor(Rgba.withAlpha(this.messageBox.getBoxBorderColor(), 100));
        }
        else {
            this.messageBox.setRect(rect);
        }
    }

    public @Override void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    public @Override void tick() {
        this.messageBox.setActive(true);
        this.messageBox.tick();
    }

    protected void submitMessage() {
        String text = this.messageBox.getValue().trim();
        if (text.isEmpty()) {
            return;
        }

        if (!this.minecraft.isCommand(text)) {
            this.minecraft.player.chat(text);
        }
    }

    protected @Override void keyPressed(char eventCharacter, int eventKey) {
        if (this.messageBox.getTickCount() < 1) {
            // Skip first frame since it includes the key that opened chat.
            return;
        }

        if (eventKey == Keyboard.KEY_ESCAPE) {
            this.minecraft.setScreen(null);
        }
        else if (eventKey == Keyboard.KEY_RETURN) {
            this.minecraft.setScreen(null);
            this.submitMessage();
        }
        else if (eventKey == Keyboard.KEY_TAB) {

        }
        else {
            this.messageBox.charTyped(eventCharacter, eventKey);

            var message = this.messageBox.getValueSpan();
            String cmdPrompt = ServerCommandSource.COMMAND_PROMPT;
            if (StringUtil.startsWith(message, cmdPrompt)) {
                var exPlayer = (ExAbstractClientPlayerEntity) this.player;
                var dispatcher = exPlayer.getCommandDispatcher();

                var reader = new StringReader(message.subSequence(cmdPrompt.length(), message.length()).toString());
                var parsed = dispatcher.parse(reader, exPlayer.createCommandSource());
                int cursor = Math.max(0, this.messageBox.getSelectionOrFull().end() - cmdPrompt.length());
                this.suggestionFuture = dispatcher.getCompletionSuggestions(parsed, cursor);
            }
            else if (StringUtil.startsWith(message, JS_PROMPT)) {
                var script = ((ExWorld) this.player.level).getScript();
                var src = message.subSequence(JS_PROMPT.length(), message.length()).toString();
                int cursor = Math.max(0, this.messageBox.getSelectionOrFull().end() - JS_PROMPT.length());

                // TODO: use futures properly...
                var eval = new Evaluator(); // TODO: store as field
                AstRoot root = eval.parseAst(src, "<cmd_suggest>", 0);
                if (root != null) {
                    this.suggestionFuture = CompletableFuture.completedFuture(
                        script.runtime().withContext(() -> Evaluator.wrap(Evaluator.suggestAtCursor(
                            script.getContext(), script.globalScope, root, new StringRange(cursor, cursor))
                            .map(k -> (Suggestion) k).sorted().toList())));
                }
            }
            else {
                this.suggestionFuture = null;
            }
        }
    }

    public @Override void render(int mouseX, int mouseY, float a) {
        try (var ui = LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            this.messageBox.render(ui);
            if (this.suggestionFuture != null && this.suggestionFuture.isDone()) {
                Suggestions suggestions = this.suggestionFuture.join();
                List<Suggestion> list = suggestions.getList();
                if (!list.isEmpty()) {
                    IntRect rect = this.messageBox.getValueRenderRect(this.font);
                    var state = ((ExTextRenderer) font).createState();
                    var background = rect.expand(new IntBorder(0, 100, list.size() * 10 + 2, -12));
                    ui.fill(background.expand(new IntBorder(1)).asFloat(), Rgba.withAlpha(Rgba.BLACK, 236));
                    state.setColor(Rgba.fromRgb8(0xe0, 0xe0, 0xe0));
                    state.setShadowToColor();
                    state.begin(ui);
                    for (int i = 0; i < list.size(); i++) {
                        Suggestion suggestion = list.get(list.size() - i - 1);
                        int y = rect.top() - i * 10 - 12;
                        state.drawText(suggestion.getText(), rect.x, y);
                        if (suggestion instanceof CodeSuggestion code) {
                            state.drawText(code.value().getString(), background.right() + 2, y);
                        }
                    }
                    state.end();
                }
            }
        }
        super.render(mouseX, mouseY, a);
    }

    protected @Override void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            String name = this.minecraft.gui.selectedName;
            if (name != null) {
                this.messageBox.append(name);
                return;
            }
        }

        this.messageBox.clicked(mouseX, mouseY, button);

        super.mouseClicked(mouseX, mouseY, button);
    }
}
