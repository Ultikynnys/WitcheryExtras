package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.GenericEvents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import baubles.api.BaublesApi;

/**
 * The full-moon shift prevention in GenericEvents.onLivingUpdate checks player.inventory.hasItem(MOON_CHARM); a moon
 * charm sitting in a Baubles charm slot would be invisible to that check and the player would be force-shifted anyway.
 * The redirections OR in a scan of the Baubles inventory. hasItem has no dev name in this environment
 * (InventoryPlayer.func_146026_a at runtime).
 */
@SuppressWarnings("UnusedMixin")
@Mixin(GenericEvents.class)
public abstract class GenericEventsMoonCharmMixin {

    @ModifyExpressionValue(
            method = "onLivingUpdate",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;hasItem (Lnet/minecraft/item/Item;)Z"))
    private boolean witcheryextras$moonCharmInBaubles(boolean hasCharmInInventory, EntityPlayer player) {
        if (hasCharmInInventory) {
            return true;
        }
        IInventory baubles = BaublesApi.getBaubles(player);
        if (baubles == null) {
            return false;
        }
        for (int slot = 0; slot < baubles.getSizeInventory(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack != null && stack.getItem() == Witchery.Items.MOON_CHARM) {
                return true;
            }
        }
        return false;
    }
}
