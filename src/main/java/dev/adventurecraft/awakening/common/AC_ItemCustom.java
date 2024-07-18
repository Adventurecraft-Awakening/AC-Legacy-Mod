package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.script.ScriptItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class AC_ItemCustom extends Item implements AC_IUseDelayItem {

    static IntArrayList loadedItemIDs = new IntArrayList();

    public String fileName;
    public String onItemUsedScript;
    private int itemUseDelay;

    private static AC_ItemCustom loadScript(File file) {
        var properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
            int id = Integer.parseInt(properties.getProperty("itemID", "-1"));
            if (id == -1) {
                Minecraft.instance.overlay.addChatMessage(String.format("ItemID for %s is unspecified", file.getName()));
            } else if (id <= 0) {
                Minecraft.instance.overlay.addChatMessage(String.format("ItemID for %s specifies a negative itemID", file.getName()));
            } else {
                if (Item.byId[id] == null) {
                    return new AC_ItemCustom(id, file.getName(), properties);
                }
                Minecraft.instance.overlay.addChatMessage(String.format("ItemID (%d) for %s is already in use by %s", id, file.getName(), Item.byId[id].toString()));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException var5) {
            Minecraft.instance.overlay.addChatMessage(String.format("ItemID for %s is specified invalidly '%s'", file.getName(), properties.getProperty("itemID")));
        }
        return null;
    }

    public AC_ItemCustom(int id, String fileName, Properties properties) {
        super(id - 256);
        this.fileName = fileName;

        String sIconIndex = properties.getProperty("iconIndex");
        if (sIconIndex != null) {
            Integer value = this.loadPropertyInt("iconIndex", sIconIndex);
            if (value != null) {
                this.setTexturePosition(value);
            }
        }

        String sMaxItemDamage = properties.getProperty("maxItemDamage");
        if (sMaxItemDamage != null) {
            Integer value = this.loadPropertyInt("maxItemDamage", sMaxItemDamage);
            if (value != null) {
                this.setDurability(value);
            }
        }

        String sMaxStackSize = properties.getProperty("maxStackSize");
        if (sMaxStackSize != null) {
            Integer value = this.loadPropertyInt("maxStackSize", sMaxStackSize);
            if (value != null) {
                this.maxStackSize = value;
            }
        }
        // configurable itemUseDelay
        this.itemUseDelay = 1; // Default
        String sItemUseDelay = properties.getProperty("itemUseDelay");
        if (sItemUseDelay != null) {
            Integer value = this.loadPropertyInt("itemUseDelay", sItemUseDelay);
            if (value != null) {
                this.itemUseDelay = value;
            }
        }

        this.setTranslationKey(properties.getProperty("name", "Unnamed"));
        this.onItemUsedScript = properties.getProperty("onItemUsedScript", "");
    }

    private Integer loadPropertyInt(String name, String value) {
        try {
            Integer parsed = Integer.parseInt(value);
            return parsed;
        } catch (NumberFormatException ex) {
            Minecraft.instance.overlay.addChatMessage(String.format("Item File '%s' Property '%s' is specified invalidly '%s'", this.fileName, name, value));
            return null;
        }
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        if (!this.onItemUsedScript.equals("")) {
            ScriptItem scriptItem = new ScriptItem(stack);
            Scriptable scope = ((ExWorld) world).getScope();
            Object jsObj = Context.javaToJS(scriptItem, scope);
            ScriptableObject.putProperty(scope, "itemUsed", jsObj);
            ((ExWorld) world).getScriptHandler().runScript(this.onItemUsedScript, scope);
        }

        return stack;
    }

    public static void loadItems(File directory) {
        for (int loadedItemID : loadedItemIDs) {
            Item.byId[loadedItemID] = null;
        }
        loadedItemIDs.clear();

        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    AC_ItemCustom item = loadScript(file);
                    if (item != null) {
                        loadedItemIDs.add(item.id);
                    }
                }
            }
        }
    }

    @Override
    public int getItemUseDelay() {
        return this.itemUseDelay;
    }
}
