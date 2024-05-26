package dev.adventurecraft.awakening.script;

import net.minecraft.entity.ItemEntity;

@SuppressWarnings("unused")
public class ScriptEntityItem extends ScriptEntity {

    ItemEntity entityItem;

    ScriptEntityItem(ItemEntity entity) {
        super(entity);
        this.entityItem = entity;
    }

    public int getAge() {
        return this.entityItem.age;
    }

    public void setAge(int value) {
        this.entityItem.age = value;
    }

    public int getHealth() {
        return this.entityItem.health;
    }

    public void setHealth(int value) {
        this.entityItem.health = value;
    }

    public ScriptItem getItem() {
        return new ScriptItem(this.entityItem.stack);
    }

    public void setItem(ScriptItem item) {
        this.entityItem.stack = item.item;
    }

    public int getPickupDelay() {
        return this.entityItem.pickupDelay;
    }

    public void setPickupDelay(int value) {
        this.entityItem.pickupDelay = value;
    }
}
