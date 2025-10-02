package dev.adventurecraft.awakening.mixin.js;

import org.mozilla.javascript.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin is likely to break expectations inside the JS engine, and may break in the future.
 * @implNote TODO: validation/testing
 */
@Mixin(
    value = Context.class,
    remap = false
)
public abstract class MixinContext {

    @Redirect(
        method = "callFunctionWithContinuations",
        at = @At(
            value = "INVOKE",
            target = "Lorg/mozilla/javascript/ScriptRuntime;hasTopCall(Lorg/mozilla/javascript/Context;)Z"
        )
    )
    private boolean noThrow(Context cx) {
        return false;
    }

    @Inject(
        method = "callFunctionWithContinuations",
        cancellable = true,
        at = @At(
            value = "INVOKE",
            target = "Lorg/mozilla/javascript/ScriptRuntime;doTopCall(Lorg/mozilla/javascript/Callable;Lorg/mozilla/javascript/Context;Lorg/mozilla/javascript/Scriptable;Lorg/mozilla/javascript/Scriptable;[Ljava/lang/Object;Z)Ljava/lang/Object;"
        )
    )
    private void doTopCall(Callable function, Scriptable scope, Object[] args, CallbackInfoReturnable<Object> cir) {
        var cx = (Context) (Object) this;
        if (ScriptRuntime.hasTopCall(cx)) {
            cir.setReturnValue(function.call(cx, scope, scope, args));
        }
    }
}
