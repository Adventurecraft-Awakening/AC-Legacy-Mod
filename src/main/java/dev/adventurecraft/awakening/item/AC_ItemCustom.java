package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.script.ScriptItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
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
                Minecraft.instance.gui.addMessage(String.format("ItemID for %s is unspecified", file.getName()));
            } else if (id <= 0) {
                Minecraft.instance.gui.addMessage(String.format("ItemID for %s specifies a negative itemID", file.getName()));
            } else {
                if (Item.items[id] == null) {
                    return new AC_ItemCustom(id, file.getName(), properties);
                }
                Minecraft.instance.gui.addMessage(String.format("ItemID (%d) for %s is already in use by %s", id, file.getName(), Item.items[id].toString()));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException var5) {
            Minecraft.instance.gui.addMessage(String.format("ItemID for %s is specified invalidly '%s'", file.getName(), properties.getProperty("itemID")));
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
                this.texture(value);
            }
        }

        String sMaxItemDamage = properties.getProperty("maxItemDamage");
        if (sMaxItemDamage != null) {
            Integer value = this.loadPropertyInt("maxItemDamage", sMaxItemDamage);
            if (value != null) {
                this.setMaxDamage(value);
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

        this.setDescriptionId(properties.getProperty("name", "Unnamed"));
        this.onItemUsedScript = properties.getProperty("onItemUsedScript", "");
    }

    private Integer loadPropertyInt(String name, String value) {
        try {
            Integer parsed = Integer.parseInt(value);
            return parsed;
        } catch (NumberFormatException ex) {
            Minecraft.instance.gui.addMessage(String.format("Item File '%s' Property '%s' is specified invalidly '%s'", this.fileName, name, value));
            return null;
        }
    }

    @Override
    public ItemInstance use(ItemInstance stack, Level world, Player player) {
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
            Item.items[loadedItemID] = null;
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
