package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityStoreRenderer extends TileEntityRenderer {

    static ItemInstance item = new ItemInstance(0, 0, 0);
    static ItemEntity eItem = new ItemEntity(null, 0.0D, 0.0D, 0.0D, item);
    static ItemRenderer renderItem = new ItemRenderer();

    public AC_TileEntityStoreRenderer() {
        renderItem.init(EntityRenderDispatcher.INSTANCE);
    }

    public void renderTileEntityStore(AC_TileEntityStore tileEntity, double x, double y, double z, float deltaTime) {

        if (tileEntity.buySupplyLeft != 0 && tileEntity.buyItemID != 0) {
            item.id = tileEntity.buyItemID;
            item.count = tileEntity.buyItemAmount;
            item.setDamage(tileEntity.buyItemDamage);

            eItem.level = tileEntity.level;
            eItem.setPos(tileEntity.x, tileEntity.y, tileEntity.z);
            renderItem.render(eItem, x + 0.5D, y + 0.125D, z + 0.5D, 0.0F, 0.0F);
        }

        AC_TriggerArea area = tileEntity.tradeTrigger;
        if (AC_DebugMode.active && area != null) {
            AC_TileEntityMinMaxRenderer.renderArea(area.min, area.max, tileEntity, x, y, z, 1, 1, 1);
        }
    }

    public void render(TileEntity entity, double x, double y, double z, float deltaTime) {
        this.renderTileEntityStore((AC_TileEntityStore) entity, x, y, z, deltaTime);
    }

    static {
        ((ExItemRenderer) renderItem).setScale(1.5F);
    }
}
