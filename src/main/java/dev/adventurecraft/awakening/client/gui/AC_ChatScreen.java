package dev.adventurecraft.awakening.client.gui;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.adventurecraft.awakening.client.gui.components.AC_EditBox;
import dev.adventurecraft.awakening.common.ServerCommandSource;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.util.DrawUtil;
import dev.adventurecraft.awakening.util.StringUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Tesselator;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class AC_ChatScreen extends Screen {

    public static final String JS_PROMPT = ""; // TODO: "$";

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

                var env = CompilerEnvirons.ideEnvirons();
                env.setXmlAvailable(false);

                AstRoot root = parseAst(env, src, "<cmd_suggest>", 0);
                if (root != null) {
                    var sugg = suggestAtCursor(script.getContext(), script.globalScope, root, cursor);
                    // TODO:
                }
            }
            else {
                this.suggestionFuture = null;
            }
        }
    }

    protected static CompletableFuture<Suggestions> suggestAtCursor(
        Context context,
        Scriptable scope,
        AstRoot root,
        int cursor
    ) {
        AstNode node = findNode(root, cursor);
        if (node == null) {
            return Suggestions.empty();
        }

        var future = new CompletableFuture<Suggestions>();

        // TODO:

        return future;
    }

    public static AstRoot parseAst(CompilerEnvirons env, String sourceString, String sourceName, int lineno) {
        var p = new Parser(env, env.getErrorReporter());
        try {
            return p.parse(sourceString, sourceName, lineno);
        }
        catch (Exception ex) {
            return null;
        }
    }

    public static @Nullable AstNode findNode(AstRoot root, int cursor) {
        var focus = new AtomicReference<AstNode>();
        root.visit(node -> {
            int pos = node.getAbsolutePosition();
            int end = pos + node.getLength();
            // TODO: improve node selection?
            if (cursor >= pos && cursor <= end) {
                focus.set(node);
            }
            return true;
        });
        return focus.get();
    }

    public @Override void render(int mouseX, int mouseY, float a) {
        /*
        this.fill(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.drawString(
            this.font,
            "> " + this.message + (this.frame / 6 % 2 == 0 ? "_" : ""),
            4,
            this.height - 12,
            14737632
        );
        */
        this.messageBox.render(this.font);

        if (this.suggestionFuture != null && this.suggestionFuture.isDone()) {
            Suggestions suggestions = this.suggestionFuture.resultNow();
            if (suggestions.isEmpty()) {

            }
            if (!suggestions.isEmpty()) {
                var list = suggestions.getList();

                IntRect rect = this.messageBox.getValueRenderRect(this.font);

                var ts = Tesselator.instance;
                var state = ((ExTextRenderer) font).createState();

                var bgRect = rect.expand(new IntBorder(0, 100, list.size() * 10 + 2, -12));

                DrawUtil.beginFill(ts);
                DrawUtil.fillRect(ts, bgRect.expand(new IntBorder(1)).asFloat(), Rgba.withAlpha(Rgba.BLACK, 236));
                DrawUtil.endFill(ts);

                state.setColor(Rgba.fromRgb8(0xe0, 0xe0, 0xe0));
                state.setShadowToColor();

                state.begin(ts);
                for (int i = 0; i < list.size(); i++) {
                    state.drawText(list.get(i).getText(), rect.x, rect.top() - i * 10 - 12);
                }
                state.end();
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
