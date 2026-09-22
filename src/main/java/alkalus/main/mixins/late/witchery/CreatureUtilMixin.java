package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.emoniph.witchery.util.CreatureUtil;

import alkalus.main.handlers.VampireTweaksHandler;

/**
 * Twilight vampires (level 10 + a GregTech MV item) are no longer considered to be in sunlight, which disables the
 * vanilla blood drain, debuffs and burning for them.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(CreatureUtil.class)
public class CreatureUtilMixin {

    @Inject(method = "isInSunlight", at = @At("HEAD"), cancellable = true, remap = false)
    private static void witcheryextras$twilightSunImmunity(EntityLivingBase entity,
            CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof EntityPlayer && VampireTweaksHandler.INSTANCE.isTwilightVampire((EntityPlayer) entity)) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
