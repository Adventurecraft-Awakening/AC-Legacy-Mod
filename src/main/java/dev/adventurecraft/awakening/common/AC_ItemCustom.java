package dev.adventurecraft.awakening.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import dev.adventurecraft.awakening.extension.item.ExItem;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import dev.adventurecraft.awakening.script.ScriptItem;
import net.minecraft.world.World;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class AC_ItemCustom extends Item {

    static IntArrayList loadedItemIDs = new IntArrayList();

    public String fileName;
    public String onItemUsedScript;

    private static AC_ItemCustom loadScript(File var0) {
        Properties var1 = new Properties();

        try {
            var1.load(new FileInputStream(var0));
            int var2 = Integer.parseInt(var1.getProperty("itemID", "-1"));
            if (var2 == -1) {
                Minecraft.instance.overlay.addChatMessage(String.format("ItemID for %s is unspecified", var0.getName()));
            } else if (var2 <= 0) {
                Minecraft.instance.overlay.addChatMessage(String.format("ItemID for %s specifies a negative itemID", var0.getName()));
            } else {
                if (Item.byId[var2] == null) {
                    return new AC_ItemCustom(var2, var0.getName(), var1);
                }

                Minecraft.instance.overlay.addChatMessage(String.format("ItemID (%d) for %s is already in use by %s", var2, var0.getName(), Item.byId[var2].toString()));
            }
        } catch (FileNotFoundException var3) {
            var3.printStackTrace();
        } catch (IOException var4) {
            var4.printStackTrace();
        } catch (NumberFormatException var5) {
            Minecraft.instance.overlay.addChatMessage(String.format("ItemID for %s is specified invalidly '%s'", var0.getName(), var1.getProperty("itemID")));
        }

        return null;
    }

    public AC_ItemCustom(int var1, String var2, Properties var3) {
        super(var1 - 256);
        this.fileName = var2;
        String var4 = var3.getProperty("iconIndex");
        if (var4 != null) {
            Integer var5 = this.loadPropertyInt("iconIndex", var4);
            if (var5 != null) {
                this.setTexturePosition(var5);
            }
        }

        String var8 = var3.getProperty("maxItemDamage");
        if (var8 != null) {
            Integer var6 = this.loadPropertyInt("maxItemDamage", var8);
            if (var6 != null) {
                this.setDurability(var6);
            }
        }

        String var9 = var3.getProperty("maxStackSize");
        if (var9 != null) {
            Integer var7 = this.loadPropertyInt("maxStackSize", var9);
            if (var7 != null) {
                this.maxStackSize = var7;
            }
        }

        this.setTranslationKey(var3.getProperty("name", "Unnamed"));
        this.onItemUsedScript = var3.getProperty("onItemUsedScript", "");
        ((ExItem) this).setItemUseDelay(1);
    }

    private Integer loadPropertyInt(String var1, String var2) {
        try {
            Integer var3 = Integer.parseInt(var2);
            return var3;
        } catch (NumberFormatException var4) {
            Minecraft.instance.overlay.addChatMessage(String.format("Item File '%s' Property '%s' is specified invalidly '%s'", this.fileName, var1, var2));
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

    public static void loadItems(File var0) {
        for (int loadedItemID : loadedItemIDs) {
            Item.byId[loadedItemID] = null;
        }

        loadedItemIDs.clear();

        if (var0.exists()) {
            File[] var6 = var0.listFiles();
            for (File var4 : var6) {
                if (var4.isFile()) {
                    AC_ItemCustom var5 = loadScript(var4);
                    if (var5 != null) {
                        loadedItemIDs.add(var5.id);
                    }
                }
            }
        }
    }
}
