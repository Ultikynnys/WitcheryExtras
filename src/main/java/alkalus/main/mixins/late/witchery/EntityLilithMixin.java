package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.emoniph.witchery.entity.EntityLilith;

import alkalus.main.handlers.VampireTweaksHandler;

/**
 * Hooks Lilith's {@code interact} before her generic "enchant any item" fallback so she can run her own quest: a
 * level-10 vampire who hands her the Wand Focus: Warding ascends to level 11, the Twilight tier. Without the hook she
 * would silently enchant the focus instead.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(EntityLilith.class)
public abstract class EntityLilithMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$wardingQuest(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (VampireTweaksHandler.INSTANCE.tryLilithWardingQuest(player)) {
            cir.setReturnValue(true);
        }
    }
}
