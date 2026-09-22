package alkalus.main.mixins.late.witchery;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.Shapeshift;

/**
 * {@code boostVampire} and {@code boostBat} are 11-entry arrays indexed by vampire level (0..10).
 * A level-11 vampire would throw ArrayIndexOutOfBoundsException; clamp the index at 10.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(Shapeshift.class)
public abstract class ShapeshiftMixin {

    @Redirect(
            method = "getStatBoost",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/emoniph/witchery/common/ExtendedPlayer;getVampireLevel()I",
                    remap = false),
            allow = 2,
            require = 2)
    private int witcheryextras$clampBoostIndex(ExtendedPlayer playerEx) {
        return Math.min(playerEx.getVampireLevel(), 10);
    }
}
