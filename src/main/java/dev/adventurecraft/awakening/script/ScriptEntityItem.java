package dev.adventurecraft.awakening.script;

import net.minecraft.world.entity.item.ItemEntity;

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
        return new ScriptItem(this.entityItem.item);
    }

    public void setItem(ScriptItem item) {
        this.entityItem.item = item.item;
    }

    public int getPickupDelay() {
        return this.entityItem.throwTime;
    }

    public void setPickupDelay(int value) {
        this.entityItem.throwTime = value;
    }
}
