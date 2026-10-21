package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.compat.LegacyModelBridge;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import dev.adventurecraft.awakening.extension.client.render.entity.ExPlayerRenderer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.*;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer extends LivingEntityRenderer implements ExPlayerRenderer {
    @Shadow private HumanoidModel humanoidModel;
    protected MixinPlayerRenderer(Model model,float shadow) { super(model,shadow); }

    @Override public void renderHand(DrawParameters parameters,RenderTexture skin) {
        this.humanoidModel.attackTime=0;
        this.humanoidModel.setupAnim(0,0,0,0,0,1F/16);
        NativeModels.part(this.humanoidModel.rightArm,ModelRenderContext.of(parameters,skin),
            parameters.object().model(),parameters.object().tint(),1F/16,false);
    }
    @Overwrite public void renderHand() {
        var entry=LegacyModelBridge.entry();
        this.renderHand(entry.context().parameters(entry.pose(),entry.tint()),entry.context().texture());
    }

    @Overwrite public void additionalRendering(Player player,float tick) {
        var entry=LegacyModelBridge.entry();
        var context=entry.context();var base=entry.pose();var tint=entry.tint();
        var textures=(ExTextureManager)Minecraft.instance.textures;
        var held=(ExHeldItemRenderer)this.entityRenderDispatcher.itemInHandRenderer;
        ItemInstance helmet=player.inventory.getArmor(3);
        if(helmet!=null && helmet.getItem().id<256) {
            var pose=((ExCuboid)this.humanoidModel.head).pose(base,1F/16,false);
            if(BlockMeshes.standard3D(Tile.tiles[helmet.id].getRenderShape()))
                pose=pose.translated(0,-.25F,0).rotated(180,0,1,0).scaled(.625F,-.625F,.625F);
            held.renderItem(player,helmet,context.parameters(pose,tint));
        }
        var skin=player.name.equals("deadmau5")?textures.skin(player.customTextureUrl,null):null;
        if(skin!=null) for(int i=0;i<2;i++) {
            float yaw=player.yRotO+(player.yRot-player.yRotO)*tick-(player.yHeadRotO+(player.yHeadRot-player.yHeadRotO)*tick);
            float pitch=player.xRotO+(player.xRot-player.xRotO)*tick;
            var pose=base.rotated(yaw,0,1,0).rotated(pitch,1,0,0).translated(.375F*(i*2-1),0,0)
                .translated(0,-.375F,0).rotated(-pitch,1,0,0).rotated(-yaw,0,1,0).scaled(1.3333334F,1.3333334F,1.3333334F);
            this.humanoidModel.ear.yRot=this.humanoidModel.head.yRot;
            this.humanoidModel.ear.xRot=this.humanoidModel.head.xRot;
            this.humanoidModel.ear.x=this.humanoidModel.ear.y=0;
            NativeModels.part(this.humanoidModel.ear,context.withTexture(skin),pose,tint,1F/16,false);
        }
        String custom=((ExPlayerEntity)player).getCloakTexture();
        var cape=custom!=null?textures.texture(custom):textures.skin(player.cloakTexture,null);
        if(cape!=null) {
            double dx=player.xCloakO+(player.xCloak-player.xCloakO)*tick-(player.xo+(player.x-player.xo)*tick);
            double dy=player.yCloakO+(player.yCloak-player.yCloakO)*tick-(player.yo+(player.y-player.yo)*tick);
            double dz=player.zCloakO+(player.zCloak-player.zCloakO)*tick-(player.zo+(player.z-player.zo)*tick);
            float yaw=player.yHeadRotO+(player.yHeadRot-player.yHeadRotO)*tick;
            double sine=Mth.sin(yaw*(float)Math.PI/180), cosine=-Mth.cos(yaw*(float)Math.PI/180);
            float lift=Math.clamp((float)dy*10,-6,32), forward=Math.max(0,(float)(dx*sine+dz*cosine)*100);
            float side=(float)(dx*cosine-dz*sine)*100;
            float bob=player.oBob+(player.bob-player.oBob)*tick;
            lift+=Mth.sin((player.walkDistO+(player.walkDist-player.walkDistO)*tick)*6)*32*bob;
            if(player.isSneaking()) lift+=25;
            var pose=base.translated(0,0,.125F).rotated(6+forward/2+lift,1,0,0)
                .rotated(side/2,0,0,1).rotated(-side/2,0,1,0).rotated(180,0,1,0);
            NativeModels.part(this.humanoidModel.cloak,context.withTexture(cape),pose,tint,1F/16,false);
        }
        var item=player.inventory.getSelected();
        if(item!=null) {
            if(player.fishing!=null) item=new ItemInstance(Item.STICK);
            var arm=((ExCuboid)this.humanoidModel.rightArm).pose(base,1F/16,false);
            held.renderItem(player,item,context.parameters(CarriedItemPose.pose(arm,item,true),tint));
        }
    }
}
