package dev.adventurecraft.awakening.world;

// TODO: many rules could be turned into script hooks e.g. disabling item usage
// TODO: some rules could be implemented as scripts e.g. sunburn_undead, melt_ice, snow_accumulation_height

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.util.TagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameRules {

    private static final Map<Key<?>, Rule<?>> INTERNAL_RULES = new HashMap<>();

    public static final Key<BoolRule> DECAY_LEAVES = register("ac:decay_leaves", false);
    public static final Key<BoolRule> SUNBURN_UNDEAD = register("ac:sunburn_undead", true);
    public static final Key<BoolRule> MELT_ICE = register("ac:melt_ice", true);
    public static final Key<BoolRule> FREEZE_WATER = register("ac:freeze_water", true); // TODO

    public static final Key<BoolRule> ALLOW_BED = register("ac:allow_bed", true);
    public static final Key<BoolRule> ALLOW_BONEMEAL = register("ac:allow_bonemeal", true);
    public static final Key<BoolRule> ALLOW_HOE = register("ac:allow_hoe", true);
    public static final Key<BoolRule> ALLOW_INVENTORY_CRAFTING = register("ac:allow_inventory_crafting", false);

    // TODO
    public static final Key<IntRule> SNOW_ACCUMULATION_HEIGHT = register("ac:snow_accumulation_height", 1, 0, 8);

    private final Map<Key<?>, Rule<?>> rules;

    public GameRules(Map<Key<?>, Rule<?>> rules) {
        this.rules = Map.copyOf(rules);
    }

    public GameRules() {
        this(INTERNAL_RULES
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
    }

    public <R extends Rule<R>> R find(Key<R> key) {
        //noinspection unchecked
        return (R) this.rules.get(key);
    }

    public Set<Key<?>> keySet() {
        return this.rules.keySet();
    }

    public void save(CompoundTag tag) {
        this.rules.forEach((key, value) -> tag.putTag(key.id(), value.toTag()));
    }

    public void load(CompoundTag tag) {
        // FIXME: unsupported rules are lost on load here

        this.rules.forEach((key, value) -> {
            // Try setting the rule if a tag was found.
            ((ExCompoundTag) tag).findTag(key.id()).ifPresent(value::setFromTag);
        });
    }

    public static Set<Map.Entry<Key<?>, Rule<?>>> internalEntries() {
        return INTERNAL_RULES.entrySet();
    }

    private static Key<BoolRule> register(String name, boolean value) {
        var key = new Key<BoolRule>(name);
        INTERNAL_RULES.put(key, new BoolRule(BoolArgumentType.bool(), value));
        return key;
    }

    private static Key<IntRule> register(String name, int value, int min, int max) {
        var key = new Key<IntRule>(name);
        INTERNAL_RULES.put(key, new IntRule(IntegerArgumentType.integer(min, max), value));
        return key;
    }

    private static Key<IntRule> register(String name, int value) {
        return register(name, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    // TODO: turn ID into registry type i.e. namespace:value
    // TODO: categories?
    public record Key<R extends Rule<R>>(String id) {
    }

    public static abstract class Rule<R extends GameRules.Rule<R>> {
        public abstract R clone();

        public abstract Object get();

        public abstract void set(Object value);

        public abstract void setFromTag(Tag tag);

        public abstract Tag toTag();

        public abstract ArgumentType<?> getArgumentType();
    }

    public static class BoolRule extends Rule<BoolRule> {
        private final BoolArgumentType argType;
        private boolean value;

        public BoolRule(BoolArgumentType argType, boolean value) {
            this.argType = argType;
            this.value = value;
        }

        public @Override BoolRule clone() {
            return new BoolRule(this.argType, this.value);
        }

        public @Override Boolean get() {
            return this.value;
        }

        public @Override void set(Object value) {
            this.value = (boolean) value;
        }

        public @Override void setFromTag(Tag tag) {
            this.set(TagUtil.toBool(tag).orElseThrow());
        }

        public @Override Tag toTag() {
            return TagUtil.fromBool(this.value);
        }

        public @Override ArgumentType<Boolean> getArgumentType() {
            return this.argType;
        }

        public boolean getBool() {
            return this.value;
        }
    }

    public static class IntRule extends Rule<IntRule> {
        private final IntegerArgumentType argType;
        private int value;

        public IntRule(IntegerArgumentType argType, int value) {
            this.argType = argType;
            this.value = value;
        }

        public @Override IntRule clone() {
            return new IntRule(this.argType, this.value);
        }

        public @Override Integer get() {
            return this.value;
        }

        public @Override void set(Object value) {
            this.value = (int) value;
        }

        public @Override void setFromTag(Tag tag) {
            this.set(TagUtil.widenToInt(tag).orElseThrow());
        }

        public @Override Tag toTag() {
            return new IntTag(this.value);
        }

        public @Override ArgumentType<Integer> getArgumentType() {
            return this.argType;
        }

        public int getInt() {
            return this.value;
        }
    }
}
