package dev.adventurecraft.awakening.mixin.js;

import org.mozilla.javascript.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin is likely to break expectations inside the JS engine, and may break in the future.
 *
 * @implNote TODO: validation/testing
 */
@Mixin(
    value = Context.class,
    remap = false
)
public abstract class MixinContext {

    @Redirect(
        method = "callFunctionWithContinuations*",
        at = @At(
            value = "INVOKE",
            target = "Lorg/mozilla/javascript/ScriptRuntime;hasTopCall(Lorg/mozilla/javascript/Context;)Z"
        )
    )
    private boolean noThrow(Context cx) {
        return false;
    }

    @Inject(
        method = "callFunctionWithContinuations(Lorg/mozilla/javascript/Script;Lorg/mozilla/javascript/Scriptable;)Ljava/lang/Object;",
        cancellable = true,
        at = @At(
            value = "INVOKE",
            target = "Lorg/mozilla/javascript/ScriptRuntime;doTopCall(Lorg/mozilla/javascript/Script;Lorg/mozilla/javascript/Context;Lorg/mozilla/javascript/Scriptable;Lorg/mozilla/javascript/Scriptable;Z)Ljava/lang/Object;"
        )
    )
    private void doTopCall(Script script, Scriptable scope, CallbackInfoReturnable<Object> cir) {
        var cx = (Context) (Object) this;
        if (ScriptRuntime.hasTopCall(cx)) {
            cir.setReturnValue(script.exec(cx, scope, scope));
        }
    }

    @Inject(
        method = "callFunctionWithContinuations(Lorg/mozilla/javascript/Callable;Lorg/mozilla/javascript/Scriptable;[Ljava/lang/Object;)Ljava/lang/Object;",
        cancellable = true,
        at = @At(
            value = "INVOKE",
            target = "Lorg/mozilla/javascript/ScriptRuntime;doTopCall(Lorg/mozilla/javascript/Callable;Lorg/mozilla/javascript/Context;Lorg/mozilla/javascript/Scriptable;Lorg/mozilla/javascript/Scriptable;[Ljava/lang/Object;Z)Ljava/lang/Object;"
        )
    )
    private void doTopCall(Callable callable, Scriptable scope, Object[] args, CallbackInfoReturnable<Object> cir) {
        var cx = (Context) (Object) this;
        if (ScriptRuntime.hasTopCall(cx)) {
            cir.setReturnValue(callable.call(cx, scope, scope, args));
        }
    }
}
