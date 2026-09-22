package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.item.ItemGeneral;

import alkalus.main.handlers.VampireTweaksHandler;

/**
 * Hooks the Lilith's Blood drink handler (the anonymous Drinkable subclass with damage 164).
 * A level-10 vampire who drinks it while wearing an MV-or-better electric helmet ascends to
 * level 11, the Twilight tier, instead of receiving the vanilla +2000 blood.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(value = ItemGeneral.class, priority = 1500)
public abstract class ItemGeneralLilithMixin {

    @Inject(method = "onEaten", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$twilightAscension(ItemStack itemstack, World world, EntityPlayer player, CallbackInfo ci) {
        // Only intercept the Lilith's Blood drinkable (sub-item damage 164).
        if (itemstack == null || itemstack.getItemDamage() != 164) {
            return;
        }
        if (VampireTweaksHandler.INSTANCE.tryGrantTwilightTier(player)) {
            world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
            ci.cancel();
        }
    }
}
