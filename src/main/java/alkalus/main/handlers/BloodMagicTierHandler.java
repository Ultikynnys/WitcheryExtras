package alkalus.main.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;

import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;

/**
 * Vampire Blood Magic upgrade (level 12 tier). A level-10+ pure-bloodline vampire who drinks Lilith's Blood a second
 * time (after the Twilight ascension) gains a Blood Magic affinity: the sacrifice dagger drains their witchery blood
 * reserve at 25 blood per HP instead of the stock 100 (which stock Witchery enforces by cancelling the health drain
 * entirely).
 * <p>
 * The {@code SacrificeKnifeUsedEvent} listener lives in {@link BloodMagicEventListener} and is only loaded when Blood
 * Magic is present, so this class never references it directly.
 */
public class BloodMagicTierHandler {

    public static final BloodMagicTierHandler INSTANCE = new BloodMagicTierHandler();

    private static final Logger LOG = LogManager.getLogger("WitcheryExtras");

    public static final int STOCK_BLOOD_PER_HP = 100;
    public static final int TIER_BLOOD_PER_HP = 25;

    /**
     * Registers the Blood Magic event listener if (and only if) Blood Magic is loaded. Called from init.
     */
    public static void registerEventListener() {
        try {
            Class.forName("WayofTime.alchemicalWizardry.api.event.SacrificeKnifeUsedEvent");
            Class<?> listener = Class.forName("alkalus.main.handlers.BloodMagicEventListener");
            try {
                listener.getField("INSTANCE");
            } catch (NoSuchFieldException e) {
                // fall through to newInstance
            }
            Object instance = listener.getField("INSTANCE") != null ? listener.getField("INSTANCE").get(null)
                    : listener.newInstance();
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            LOG.info("WitcheryExtras: Blood Magic detected, Blood Magic tier listener active");
        } catch (ClassNotFoundException e) {
            LOG.info("WitcheryExtras: Blood Magic not present, Blood Magic tier listener disabled");
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOG.warn("WitcheryExtras: failed to register Blood Magic tier listener", e);
        }
    }

    /**
     * Grants the Blood Magic affinity: a Twilight vampire (level 11, Twilight flag set) who drinks Lilith's Blood again
     * has their blood permanently attuned to Blood Magic's altar work instead of receiving the vanilla +2000 blood.
     * Requires the central gate to pass.
     */
    public boolean tryGrantBloodMagicTier(EntityPlayer player) {
        if (player.worldObj.isRemote || player.capabilities.isCreativeMode) {
            return false;
        }
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || !WitcheryUpgrades.canUseVampireUpgrade(player)) {
            return false;
        }
        WitcheryUpgradeHelper we = (WitcheryUpgradeHelper) ex;
        if (ex.getVampireLevel() < 11 || !we.witcheryExtras$isTwilight() || we.witcheryExtras$isBloodMagic()) {
            return false;
        }
        we.witcheryExtras$setBloodMagic(true);
        ex.setVampireLevel(ex.getVampireLevel() + 1);
        ChatUtil.sendTranslated(
                EnumChatFormatting.DARK_RED,
                player,
                "witcheryextras.upgrade.bloodmagic.ritual",
                new Object[0]);
        ParticleEffect.REDDUST.send(SoundEffect.RANDOM_LEVELUP, player, 1.0D, 2.0D, 64);
        LOG.info(
                "WitcheryExtras: {} attained the Blood Magic affinity (vampire level {})",
                player.getCommandSenderName(),
                ex.getVampireLevel());
        return true;
    }

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
        int rebate = (int) (event.healthDrained * (STOCK_BLOOD_PER_HP - TIER_BLOOD_PER_HP));
        if (rebate > 0 && ex.getBloodPower() > 0) {
            ex.increaseBloodPower(Math.min(rebate, ex.getMaxBloodPower() - ex.getBloodPower()));
            LOG.debug("WitcheryExtras: Blood Magic tier rebate {} blood for {}", rebate, player.getCommandSenderName());
        }
    }
}
