package alkalus.main.core;

import net.minecraft.entity.player.EntityPlayer;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.Config;

/**
 * Central gate for every WitcheryExtras upgrade (Twilight, Blood Magic, Wereman). An upgrade
 * only applies when the player:
 * <ul>
 * <li>has reached level {@value #MIN_UPGRADE_LEVEL} of the owning class (the original quest
 *     flow below 10 stays untouched), and</li>
 * <li>is not a vampire/werewolf hybrid - hybrids are excluded even when the Witchery hybrid
 *     config is enabled, because post-10 upgrades assume a single pure bloodline.</li>
 * </ul>
 */
public final class WitcheryUpgrades {

    public static final int MIN_UPGRADE_LEVEL = WitcheryUpgradeHelper.WE_MIN_LEVEL_FOR_UPGRADES;

    private WitcheryUpgrades() {}

    public static boolean isHybrid(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        return ex != null
            && ex.isVampire()
            && ex.getWerewolfLevel() > 0
            && Config.instance().allowVampireWolfHybrids;
    }

    /** Vampire upgrades (Twilight, Blood Magic). */
    public static boolean canUseVampireUpgrade(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        return ex != null
            && !player.capabilities.isCreativeMode
            && ex.isVampire()
            && ex.getVampireLevel() >= MIN_UPGRADE_LEVEL
            && ex.getWerewolfLevel() == 0;
    }

    /** Werewolf upgrades (Wereman). */
    public static boolean canUseWerewolfUpgrade(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        return ex != null
            && !player.capabilities.isCreativeMode
            && ex.getWerewolfLevel() >= MIN_UPGRADE_LEVEL
            && !ex.isVampire();
    }
}
