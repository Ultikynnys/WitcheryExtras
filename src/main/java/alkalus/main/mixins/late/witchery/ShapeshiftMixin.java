package alkalus.main.mixins.late.witchery;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.util.TransformCreature;

import net.minecraft.entity.player.EntityPlayer;

/**
 * {@code boostVampire} and {@code boostBat} are 11-entry arrays indexed by vampire level (0..10).
 * A level-11 vampire would throw ArrayIndexOutOfBoundsException; recompute the boost for
 * level &gt; 10 using the level-10 entry (level 11 keeps level-10 stats, plus our handler adds
 * its own regen tier).
 */
@SuppressWarnings("UnusedMixin")
@Mixin(Shapeshift.class)
public abstract class ShapeshiftMixin {

    @Inject(method = "getStatBoost", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$clampBoostIndex(EntityPlayer player, ExtendedPlayer playerEx, CallbackInfoReturnable<Shapeshift.StatBoost> cir) {
        if (playerEx.getVampireLevel() > 10) {
            TransformCreature creature = playerEx.getCreatureType();
            if (creature == TransformCreature.BAT) {
                cir.setReturnValue(this.boostBat[10]);
            } else if (playerEx.isVampire()) {
                cir.setReturnValue(this.boostVampire[10]);
            }
        }
    }

    @Shadow(remap = false)
    public Shapeshift.StatBoost[] boostBat;

    @Shadow(remap = false)
    public Shapeshift.StatBoost[] boostVampire;
}
