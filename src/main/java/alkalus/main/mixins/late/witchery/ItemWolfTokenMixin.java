package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.item.ItemWolfToken;
import com.emoniph.witchery.util.ChatUtil;

import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;

/**
 * Creative Bat/Wolf Token level cycling extended past 10: crossing the stock quest ladder grants the WitcheryExtras
 * upgrade flags in a fixed order, so creative tokens still hand out every upgrade. Vampire: Twilight -&gt; Blood Magic.
 * Werewolf: Wereman. The central {@link WitcheryUpgrades} gate enforces a minimum class level of 10 and excludes
 * hybrids, so tokens can never bypass those rules.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ItemWolfToken.class)
public abstract class ItemWolfTokenMixin {

    @Unique
    private static final int WE_VAMPIRE_CAP = 12;

    @Unique
    private static final int WE_WEREWOLF_CAP = 11;

    @Inject(method = "onUsingTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$extendedCycle(ItemStack stack, EntityPlayer player, int countdown, CallbackInfo ci) {
        if (player.worldObj.isRemote || countdown != 1) {
            return;
        }
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null) {
            return;
        }
        WitcheryUpgradeHelper we = (WitcheryUpgradeHelper) ex;
        int level;
        if (player.isSneaking()) {
            level = ex.getVampireLevel() + 1;
            if (level > WE_VAMPIRE_CAP) {
                level = 0;
                we.witcheryExtras$setTwilight(false);
                we.witcheryExtras$setBloodMagic(false);
            }
            if (level > WitcheryUpgrades.MIN_UPGRADE_LEVEL && WitcheryUpgrades.canUseVampireUpgrade(player)) {
                if (level == 11) {
                    we.witcheryExtras$setTwilight(true);
                    ChatUtil.sendTranslated(
                            EnumChatFormatting.LIGHT_PURPLE,
                            player,
                            "witcheryextras.upgrade.twilight",
                            new Object[0]);
                } else if (level == 12) {
                    we.witcheryExtras$setBloodMagic(true);
                    ChatUtil.sendTranslated(
                            EnumChatFormatting.LIGHT_PURPLE,
                            player,
                            "witcheryextras.upgrade.bloodmagic",
                            new Object[0]);
                }
            }
            ex.setVampireLevel(level);
            ChatUtil.sendTranslated(
                    EnumChatFormatting.GREEN,
                    player,
                    "witchery.vampire.setlevel",
                    new Object[] { Integer.valueOf(level).toString() });
        } else {
            level = ex.getWerewolfLevel() + 1;
            if (level > WE_WEREWOLF_CAP) {
                level = 0;
                we.witcheryExtras$setWereman(false);
            }
            if (level > WitcheryUpgrades.MIN_UPGRADE_LEVEL && WitcheryUpgrades.canUseWerewolfUpgrade(player)) {
                we.witcheryExtras$setWereman(true);
                ChatUtil.sendTranslated(
                        EnumChatFormatting.LIGHT_PURPLE,
                        player,
                        "witcheryextras.upgrade.wereman",
                        new Object[0]);
            }
            ex.setWerewolfLevel(level);
            ChatUtil.sendTranslated(
                    EnumChatFormatting.GREEN,
                    player,
                    "witchery.werewolf.setlevel",
                    new Object[] { Integer.valueOf(level).toString() });
        }
        ci.cancel();
    }
}
