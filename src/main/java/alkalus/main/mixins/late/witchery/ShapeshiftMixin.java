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
 * {@code boostWolf}, {@code boostWolfman}, {@code boostBat}, {@code boostVampire} are 11-entry
 * arrays indexed by class level (0..10). Beyond level 10 the indexes would throw
 * ArrayIndexOutOfBoundsException; recompute the boost for level &gt; 10 from the level-10 entry
 * (post-10 upgrades add perks of their own, not bigger stat tables).
 */
@SuppressWarnings("UnusedMixin")
@Mixin(Shapeshift.class)
public abstract class ShapeshiftMixin {

    @Inject(method = "getStatBoost", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$clampBoostIndex(EntityPlayer player, ExtendedPlayer playerEx, CallbackInfoReturnable<Shapeshift.StatBoost> cir) {
        int wolf = Math.min(playerEx.getWerewolfLevel(), 10);
        int vampire = Math.min(playerEx.getVampireLevel(), 10);
        TransformCreature creature = playerEx.getCreatureType();
        if (creature == TransformCreature.WOLF) {
            cir.setReturnValue(this.boostWolf[wolf]);
        } else if (creature == TransformCreature.WOLFMAN) {
            cir.setReturnValue(this.boostWolfman[wolf]);
        } else if (creature == TransformCreature.BAT) {
            cir.setReturnValue(this.boostBat[vampire]);
        } else if (playerEx.isVampire()) {
            cir.setReturnValue(this.boostVampire[vampire]);
        }
    }

    @Shadow(remap = false)
    public Shapeshift.StatBoost[] boostBat;

    @Shadow(remap = false)
    public Shapeshift.StatBoost[] boostVampire;

    @Shadow(remap = false)
    public Shapeshift.StatBoost[] boostWolf;

    @Shadow(remap = false)
    public Shapeshift.StatBoost[] boostWolfman;
}
