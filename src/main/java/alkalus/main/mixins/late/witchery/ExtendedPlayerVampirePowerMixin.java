package alkalus.main.mixins.late.witchery;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.emoniph.witchery.common.ExtendedPlayer;

/**
 * {@code VampirePower.levels} is an 11-entry array indexed by vampire level (0..10). A level-11 vampire
 * would crash the power-selection wheel; clamp the index at 10 (level 11 uses the same powers as 10).
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ExtendedPlayer.class)
public abstract class ExtendedPlayerVampirePowerMixin {

    @Inject(method = "getMaxAvailablePowerOrdinal", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$clampLevelsIndex(CallbackInfoReturnable<Integer> cir) {
        ExtendedPlayer self = (ExtendedPlayer) (Object) this;
        if (self.getVampireLevel() > 10) {
            cir.setReturnValue(4);
        }
    }
}
