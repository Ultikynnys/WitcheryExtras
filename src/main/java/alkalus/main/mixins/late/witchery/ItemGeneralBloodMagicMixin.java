package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.item.ItemGeneral;

import alkalus.main.handlers.BloodMagicTierHandler;

/**
 * Hooks the Witchery drink handler for Lilith's Blood (sub-item damage 164). A Twilight
 * vampire who drinks it a second time attains the Blood Magic affinity (level 12) instead of
 * the vanilla +2000 blood; every other case falls through untouched.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(value = ItemGeneral.class, priority = 1500)
public abstract class ItemGeneralBloodMagicMixin {

    @Inject(method = "onEaten", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$bloodMagicAscension(ItemStack itemstack, World world, EntityPlayer player, CallbackInfo ci) {
        if (itemstack == null || itemstack.getItemDamage() != 164) {
            return;
        }
        if (BloodMagicTierHandler.INSTANCE.tryGrantBloodMagicTier(player)) {
            world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
            ci.cancel();
        }
    }
}
