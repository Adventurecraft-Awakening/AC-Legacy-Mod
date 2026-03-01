package dev.adventurecraft.awakening.common;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.locale.I18n;

import java.util.Deque;
import java.util.HashMap;

public class CommandDescriptions {

    private final HashMap<Object, String> map = new HashMap<>();

    // TODO: migrate all descriptions to lang file
    @Deprecated
    public <T> T attach(T value, String description) {
        this.map.put(value, description);
        return value;
    }

    public <S> String getDescription(Deque<CommandNode<S>> path) {
        var topNode = path.peek();
        if (topNode == null) {
            return "";
        }

        String literal = this.map.get(topNode.getCommand());
        if (literal != null) {
            return literal;
        }

        // Build up path from nodes.
        var pathBuilder = new StringBuilder();
        pathBuilder.append("command");
        var iter = path.descendingIterator();
        while (iter.hasNext()) {
            var node = iter.next();
            pathBuilder.append('.');
            pathBuilder.append(node.getName());
        }
        pathBuilder.append(".desc");

        // TODO: add lookup helper with fallback value other than key...
        String key = pathBuilder.toString();
        String result = I18n.getInstance().get(key);
        if (key.equals(result)) {
            return "";
        }
        return result;
    }
}
