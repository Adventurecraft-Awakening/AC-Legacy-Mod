package dev.adventurecraft.awakening.mixin.js;

import com.llamalad7.mixinextras.sugar.Local;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ErrorNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = Parser.class,
    remap = false
)
public abstract class MixinParser {

    @Shadow
    protected abstract int lineNumber();

    @Shadow
    protected abstract int columnNumber();

    @Inject(
        method = "propertyAccess",
        at = {
            @At(
                value = "RETURN",
                ordinal = 1
            ), //
            @At(
                value = "RETURN",
                ordinal = 2
            ), //
            @At(
                value = "RETURN",
                ordinal = 4
            ), //
            @At(
                value = "RETURN",
                ordinal = 6
            ), //
            @At(
                value = "RETURN",
                ordinal = 7
            )
        },
        cancellable = true
    )
    private void tryFixupErroredName(
        int tt,
        AstNode pn,
        boolean isOptionalChain,
        CallbackInfoReturnable<AstNode> cir,
        @Local(name = "lineno") int lineno,
        @Local(name = "dotPos") int dotPos,
        @Local(name = "column") int column,
        @Local(name = "token") int token
    ) {
        if (token == Token.EOF || token == Token.RB || token == Token.RP || token == Token.RC || token == Token.SEMI ||
            token == Token.COMMA) {
            var error = (ErrorNode) cir.getReturnValue();

            int offset = token == Token.EOF ? 1 : 0;
            var name = new Name(error.getPosition() + offset, "");
            name.setLineColumnNumber(this.lineNumber(), this.columnNumber());

            var pg = new PropertyGet(pn, name, dotPos);
            pg.setLineColumnNumber(lineno, column);
            cir.setReturnValue(pg);
        }
    }
}
