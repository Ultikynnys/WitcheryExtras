package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.GenericEvents;

import alkalus.main.core.WitcheryUpgrades;

/**
 * Stock updateWerewolfEffects sweeps every 40 ticks and drops the whole wolfman loadout (slots 0-4). This re-implements
 * that sweep for upgraded werewolves so Greater Form Control keeps the held item (slot 0) and Form Mastery keeps armor
 * (slots 1-4). Wolf form and non-upgraded werewolves fall through to the stock body.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(GenericEvents.class)
public abstract class WerewolfEffectsMixin {

    @Inject(method = "updateWerewolfEffects", at = @At("HEAD"), cancellable = true, remap = false)
    private static void witcheryextras$keepUpgradedSlots(EntityPlayer player, boolean isWolfman, CallbackInfo ci) {
        if (!isWolfman) {
            return;
        }
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null) {
            return;
        }
        boolean holdItems = WitcheryUpgrades.hasGreaterFormControl(player);
        boolean wearArmor = WitcheryUpgrades.hasFormMastery(player);
        if (!holdItems && !wearArmor) {
            return;
        }
        player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 400, 0, true));
        if (player.isPotionActive(Potion.poison)) {
            player.removePotionEffect(Potion.poison.id);
        }
        for (int slot = 0; slot <= 4; ++slot) {
            if (slot == 0 && holdItems) {
                continue;
            }
            if (slot >= 1 && wearArmor) {
                continue;
            }
            ItemStack stack = player.getEquipmentInSlot(slot);
            if (stack != null && stack.getItem() != Witchery.Items.MOON_CHARM
                    && (player.openContainer == null || player.openContainer.windowId == 0 || slot != 0)) {
                player.entityDropItem(stack, 1.0F);
                player.setCurrentItemOrArmor(slot, (ItemStack) null);
            }
        }
        ci.cancel();
    }
}
