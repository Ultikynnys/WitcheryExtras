package alkalus.main.handlers;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TransformCreature;

import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;

/**
 * Werewolf post-10 ascension rite, parameterised so the same two-stage "shift into the wolfman form inside the circle"
 * ritual grants either Greater Form Control (at werewolf level 10) or Form Mastery (at werewolf level 11). All chat
 * text is keyed off the rite id, so each rite carries its own flavour.
 */
public class WerewolfProgressionRite extends Rite {

    private final String id;
    private final int requiredLevel;
    private final boolean mastery;

    public WerewolfProgressionRite(String id, int requiredLevel, boolean mastery) {
        this.id = id;
        this.requiredLevel = requiredLevel;
        this.mastery = mastery;
    }

    @Override
    public void addSteps(ArrayList steps, int initialStage) {
        steps.add(new Step());
    }

    private String key(String suffix) {
        return "witcheryextras.rite." + this.id + "." + suffix;
    }

    private String pendingKey() {
        return "WitcheryExtrasRitePending_" + this.id;
    }

    private boolean alreadyGranted(WitcheryUpgradeHelper we) {
        return this.mastery ? we.witcheryExtras$hasFormMastery() : we.witcheryExtras$hasGreaterFormControl();
    }

    private void grant(ExtendedPlayer ex, WitcheryUpgradeHelper we) {
        if (this.mastery) {
            we.witcheryExtras$setFormMastery(true);
        } else {
            we.witcheryExtras$setGreaterFormControl(true);
        }
        ex.setWerewolfLevel(ex.getWerewolfLevel() + 1);
    }

    private final class Step extends RitualStep {

        Step() {
            super(true);
        }

        @Override
        public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks,
                BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
            if (world.isRemote) {
                return RitualStep.Result.COMPLETED;
            }
            if (ticks % 20L != 0L) {
                return RitualStep.Result.STARTING;
            }
            EntityPlayer player = ritual.getInitiatingPlayer(world);
            if (player == null) {
                return RitualStep.Result.ABORTED_REFUND;
            }
            ExtendedPlayer ex = ExtendedPlayer.get(player);
            if (ex == null || !WitcheryUpgrades.canUseWerewolfUpgrade(player)) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, key("refused"), new Object[0]);
                return RitualStep.Result.ABORTED_REFUND;
            }
            WitcheryUpgradeHelper we = (WitcheryUpgradeHelper) ex;
            if (alreadyGranted(we)) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, key("already"), new Object[0]);
                return RitualStep.Result.COMPLETED;
            }
            if (ex.getWerewolfLevel() < requiredLevel) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, key("notready"), new Object[0]);
                return RitualStep.Result.ABORTED_REFUND;
            }
            if (player.getEntityData().getBoolean(pendingKey())) {
                grant(ex, we);
                player.getEntityData().setBoolean(pendingKey(), false);
                ChatUtil.sendTranslated(EnumChatFormatting.LIGHT_PURPLE, player, key("completed"), new Object[0]);
                ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_LEVELUP, player, 1.0D, 2.0D, 64);
                return RitualStep.Result.COMPLETED;
            }
            if (ex.getCreatureType() != TransformCreature.WOLFMAN) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, key("shift"), new Object[0]);
                return RitualStep.Result.ABORTED_REFUND;
            }
            player.getEntityData().setBoolean(pendingKey(), true);
            ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, key("stage2"), new Object[0]);
            return RitualStep.Result.STARTING;
        }
    }
}
