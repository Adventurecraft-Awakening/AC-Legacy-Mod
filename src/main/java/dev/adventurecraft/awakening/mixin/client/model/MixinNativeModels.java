package dev.adventurecraft.awakening.mixin.client.model;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.model.ExModel;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Explicit vanilla model recipes; the animation methods continue to own part angles. */
public final class MixinNativeModels {
    @Mixin(Model.class) public abstract static class Base implements ExModel { }

    @Mixin(HumanoidModel.class)
    public abstract static class Humanoid extends Model implements ExModel {
        @Shadow private ModelPart head;
        @Shadow private ModelPart body;
        @Shadow private ModelPart rightArm;
        @Shadow private ModelPart leftArm;
        @Shadow private ModelPart rightLeg;
        @Shadow private ModelPart leftLeg;
        @Shadow private ModelPart hair;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.head,context,pose,tint,scale,false);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            NativeModels.part(this.rightArm,context,pose,tint,scale,false);
            NativeModels.part(this.leftArm,context,pose,tint,scale,false);
            NativeModels.part(this.rightLeg,context,pose,tint,scale,false);
            NativeModels.part(this.leftLeg,context,pose,tint,scale,false);
            NativeModels.part(this.hair,context,pose,tint,scale,false);
        }
    }

    @Mixin(QuadrupedModel.class)
    public abstract static class Quadruped extends Model implements ExModel {
        @Shadow private ModelPart head;
        @Shadow private ModelPart body;
        @Shadow private ModelPart leg0;
        @Shadow private ModelPart leg1;
        @Shadow private ModelPart leg2;
        @Shadow private ModelPart leg3;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.head,context,pose,tint,scale,false);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            NativeModels.part(this.leg0,context,pose,tint,scale,false);
            NativeModels.part(this.leg1,context,pose,tint,scale,false);
            NativeModels.part(this.leg2,context,pose,tint,scale,false);
            NativeModels.part(this.leg3,context,pose,tint,scale,false);
        }
    }

    @Mixin(ChickenModel.class)
    public abstract static class Chicken extends Model implements ExModel {
        @Shadow private ModelPart head;
        @Shadow private ModelPart beak;
        @Shadow private ModelPart redThing;
        @Shadow private ModelPart body;
        @Shadow private ModelPart leg0;
        @Shadow private ModelPart leg1;
        @Shadow private ModelPart wing0;
        @Shadow private ModelPart wing1;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.head,context,pose,tint,scale,false);
            NativeModels.part(this.beak,context,pose,tint,scale,false);
            NativeModels.part(this.redThing,context,pose,tint,scale,false);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            NativeModels.part(this.leg0,context,pose,tint,scale,false);
            NativeModels.part(this.leg1,context,pose,tint,scale,false);
            NativeModels.part(this.wing0,context,pose,tint,scale,false);
            NativeModels.part(this.wing1,context,pose,tint,scale,false);
        }
    }

    @Mixin(CreeperModel.class)
    public abstract static class Creeper extends Model implements ExModel {
        @Shadow private ModelPart head;
        @Shadow private ModelPart body;
        @Shadow private ModelPart leg0;
        @Shadow private ModelPart leg1;
        @Shadow private ModelPart leg2;
        @Shadow private ModelPart leg3;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.head,context,pose,tint,scale,false);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            NativeModels.part(this.leg0,context,pose,tint,scale,false);
            NativeModels.part(this.leg1,context,pose,tint,scale,false);
            NativeModels.part(this.leg2,context,pose,tint,scale,false);
            NativeModels.part(this.leg3,context,pose,tint,scale,false);
        }
    }

    @Mixin(SpiderModel.class)
    public abstract static class Spider extends Model implements ExModel {
        @Shadow private ModelPart head;
        @Shadow private ModelPart body0;
        @Shadow private ModelPart body1;
        @Shadow private ModelPart leg0;
        @Shadow private ModelPart leg1;
        @Shadow private ModelPart leg2;
        @Shadow private ModelPart leg3;
        @Shadow private ModelPart leg4;
        @Shadow private ModelPart leg5;
        @Shadow private ModelPart leg6;
        @Shadow private ModelPart leg7;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.head,context,pose,tint,scale,false);
            NativeModels.part(this.body0,context,pose,tint,scale,false);
            NativeModels.part(this.body1,context,pose,tint,scale,false);
            NativeModels.part(this.leg0,context,pose,tint,scale,false);
            NativeModels.part(this.leg1,context,pose,tint,scale,false);
            NativeModels.part(this.leg2,context,pose,tint,scale,false);
            NativeModels.part(this.leg3,context,pose,tint,scale,false);
            NativeModels.part(this.leg4,context,pose,tint,scale,false);
            NativeModels.part(this.leg5,context,pose,tint,scale,false);
            NativeModels.part(this.leg6,context,pose,tint,scale,false);
            NativeModels.part(this.leg7,context,pose,tint,scale,false);
        }
    }

    @Mixin(WolfModel.class)
    public abstract static class Wolf extends Model implements ExModel {
        @Shadow private ModelPart head;
        @Shadow private ModelPart body;
        @Shadow private ModelPart leg0;
        @Shadow private ModelPart leg1;
        @Shadow private ModelPart leg2;
        @Shadow private ModelPart leg3;
        @Shadow private ModelPart field_2095;
        @Shadow private ModelPart field_2096;
        @Shadow private ModelPart field_2097;
        @Shadow private ModelPart tail;
        @Shadow private ModelPart upperBody;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.head,context,pose,tint,scale,true);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            NativeModels.part(this.leg0,context,pose,tint,scale,false);
            NativeModels.part(this.leg1,context,pose,tint,scale,false);
            NativeModels.part(this.leg2,context,pose,tint,scale,false);
            NativeModels.part(this.leg3,context,pose,tint,scale,false);
            NativeModels.part(this.field_2095,context,pose,tint,scale,true);
            NativeModels.part(this.field_2096,context,pose,tint,scale,true);
            NativeModels.part(this.field_2097,context,pose,tint,scale,true);
            NativeModels.part(this.tail,context,pose,tint,scale,true);
            NativeModels.part(this.upperBody,context,pose,tint,scale,false);
        }
    }

    @Mixin(CowModel.class)
    public abstract static class Cow extends QuadrupedModel implements ExModel {
        protected Cow() { super(0,0); }
        @Shadow private ModelPart horn1;
        @Shadow private ModelPart horn2;
        @Shadow private ModelPart udder;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            NativeModels.quadruped(this,context,pose,tint,time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.horn1,context,pose,tint,scale,false);
            NativeModels.part(this.horn2,context,pose,tint,scale,false);
            NativeModels.part(this.udder,context,pose,tint,scale,false);
        }
    }

    @Mixin(SlimeModel.class)
    public abstract static class Slime extends Model implements ExModel {
        @Shadow private ModelPart cube;
        @Shadow private ModelPart eye0;
        @Shadow private ModelPart eye1;
        @Shadow private ModelPart mouth;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.cube,context,pose,tint,scale,false);
            NativeModels.part(this.eye0,context,pose,tint,scale,false);
            NativeModels.part(this.eye1,context,pose,tint,scale,false);
            NativeModels.part(this.mouth,context,pose,tint,scale,false);
        }
    }

    @Mixin(GhastModel.class)
    public abstract static class Ghast extends Model implements ExModel {
        @Shadow private ModelPart body;
        @Shadow private ModelPart[] tentacles;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            for(int i=0;i<this.tentacles.length;i++) NativeModels.part(this.tentacles[i],context,pose,tint,scale,false);
        }
    }

    @Mixin(SquidModel.class)
    public abstract static class Squid extends Model implements ExModel {
        @Shadow private ModelPart body;
        @Shadow private ModelPart[] tentacles;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.setupAnim(time,strength,bob,yaw,pitch,scale);
            NativeModels.part(this.body,context,pose,tint,scale,false);
            for(int i=0;i<this.tentacles.length;i++) NativeModels.part(this.tentacles[i],context,pose,tint,scale,false);
        }
    }

    @Mixin(BoatModel.class)
    public abstract static class Boat extends Model implements ExModel {
        @Shadow private ModelPart[] cubes;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            for(int i=0;i<5;i++) NativeModels.part(this.cubes[i],context,pose,tint,scale,false);
        }
    }

    @Mixin(MinecartModel.class)
    public abstract static class Minecart extends Model implements ExModel {
        @Shadow private ModelPart[] cubes;
        @Override public boolean ac$nativeModel() { return true; }
        @Override public void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
            float time,float strength,float bob,float yaw,float pitch,float scale) {
            this.cubes[5].y=4-bob;
            for(int i=0;i<6;i++) NativeModels.part(this.cubes[i],context,pose,tint,scale,false);
        }
    }
}
