package alkalus.main.handlers;

import net.minecraft.entity.player.EntityPlayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emoniph.witchery.common.ExtendedPlayer;

import WayofTime.alchemicalWizardry.api.event.SacrificeKnifeUsedEvent;
import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * The {@code SacrificeKnifeUsedEvent} listener for the Blood Magic tier. Split from {@link BloodMagicTierHandler} so
 * this class (which references Blood Magic types) is only classloaded when Blood Magic is actually present - the
 * handler registers it reflectively. Runs at LOW priority so it applies after Witchery's own knife handler charged
 * 100/HP.
 */
public class BloodMagicEventListener {

    public static final BloodMagicEventListener INSTANCE = new BloodMagicEventListener();

    private static final Logger LOG = LogManager.getLogger("WitcheryExtras");

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSacrificeKnifeUsed(SacrificeKnifeUsedEvent event) {
        EntityPlayer player = event.player;
        if (player == null || player.worldObj.isRemote || !WitcheryUpgrades.canUseVampireUpgrade(player)) {
            return;
        }
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || !((WitcheryUpgradeHelper) ex).witcheryExtras$isBloodMagic()) {
            return;
        }
        // Witchery's handler already cancelled the health drain and charged 100/HP.
        // Rebate the difference so the tier pays 25/HP.
        int rebate = (int) (event.healthDrained
                * (BloodMagicTierHandler.STOCK_BLOOD_PER_HP - BloodMagicTierHandler.TIER_BLOOD_PER_HP));
        if (rebate > 0 && ex.getBloodPower() > 0) {
            ex.increaseBloodPower(Math.min(rebate, ex.getMaxBloodPower() - ex.getBloodPower()));
            LOG.debug("WitcheryExtras: Blood Magic tier rebate {} blood for {}", rebate, player.getCommandSenderName());
        }
    }
}
