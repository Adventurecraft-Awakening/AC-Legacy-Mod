package dev.adventurecraft.awakening.js;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.dom.Node;
import dev.adventurecraft.awakening.text.JaroWinklerSimilarity;
import dev.adventurecraft.awakening.util.StringUtil;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

// TODO: use futures
public class Evaluator {

    private final CompilerEnvirons env;

    public Evaluator(CompilerEnvirons env) {
        this.env = env;
    }

    public Evaluator() {
        var env = CompilerEnvirons.ideEnvirons();
        env.setXmlAvailable(false);
        this(env);
    }

    public AstRoot parseAst(String sourceString, String sourceName, int lineno) {
        return parseAst(this.env, sourceString, sourceName, lineno);
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

    public static Suggestions wrap(List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return Suggestions.empty().resultNow();
        }
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (Suggestion suggestion : suggestions) {
            start = Math.min(suggestion.getRange().getStart(), start);
            end = Math.max(suggestion.getRange().getEnd(), end);
        }
        var range = new StringRange(start, end);
        //Set<Suggestion> texts = new HashSet<>();
        //for (Suggestion suggestion : suggestions) {
        //    texts.add(suggestion.expand(command, range));
        //}
        //List<Suggestion> sorted = new ArrayList<>(texts);
        //sorted.sort((a, b) -> a.compareToIgnoreCase(b));
        return new Suggestions(range, suggestions);
    }

    public static Stream<CodeSuggestion> suggestAtCursor(
        Context context,
        Scriptable scope,
        AstRoot root,
        StringRange range
    ) {
        AstNode node = findNode(root, range.getStart());

        // TODO: turn node under cursor into result too?

        Scriptable enclosure = null;
        if (node == root) {
            enclosure = scope;
        }
        else if (node instanceof ErrorNode err) {
            AstNode parent = err.getParent();
            if (parent instanceof InfixExpression infix) {
                enclosure = scope;
            }
            else if (parent instanceof FunctionCall call) {
                enclosure = scope; // TODO: suggest valid params?
            }
        }
        else if (node != null) {
            AstNode parent = node.getParent();
            if (parent instanceof InfixExpression infix) {
                enclosure = evalString(context, scope, infix.getLeft().toSource());
            }
            else if (parent instanceof ExpressionStatement expr) {
                if (expr.getParent() == root) {
                    enclosure = scope;
                }
            }
            else if (parent == root) {
                enclosure = scope;
            }
        }

        String pattern = null;
        if (node instanceof Name name) {
            pattern = name.getIdentifier();
        }

        if (enclosure == null) {
            return Stream.empty();
        }

        Scriptable target = enclosure;
        var ids = new ObjectArraySet<>(Arrays.asList(target.getIds()));
        if (target instanceof NativeJavaObject) {
            ids.remove("class");
            ids.remove("getClass");
        }

        return suggestKeys(
            ids.stream().map(k -> {
                // TODO: iterate Slots directly
                Object value;
                try {
                    if (k instanceof String s) {
                        value = target.get(s, target);
                    }
                    else {
                        value = target.get((Integer) k, target);
                    }
                }
                catch (Exception ex) {
                    value = ex;
                }

                String valueText = "null";
                if (value instanceof Wrapper wrapper) {
                    // TODO: don't naively call toString on big collections
                    valueText = wrapper.unwrap().toString();
                }
                else if (value != null) {
                    valueText = value.toString();
                }

                return new CodeSuggestion(range, k.toString(), Node.text(valueText));
            }), pattern
        );
    }

    static Stream<CodeSuggestion> suggestKeys(Stream<CodeSuggestion> values, String pattern) {
        if (!StringUtil.isNullOrEmpty(pattern)) {
            String p = pattern.toLowerCase(Locale.ROOT);
            var sim = new JaroWinklerSimilarity();
            return values
                .filter(v -> v.getText().toLowerCase(Locale.ROOT).contains(p))
                .sequential()
                .map(v -> new CodeSuggestion(
                    v.getRange(),
                    v.getText(),
                    v.value(),
                    sim.matchScore(v.getText(), pattern)
                ));
        }
        return values;
    }

    private static Scriptable evalString(Context context, Scriptable scope, String source) {
        try {
            Object result = context.evaluateString(scope, source, null, 0, null);
            return context.getWrapFactory().wrapNewObject(context, scope, result);
        }
        catch (Exception ex) {
            ACMod.JS_LOGGER.info("Suggestion error: {}", ex.getMessage());
        }
        return null;
    }
}
