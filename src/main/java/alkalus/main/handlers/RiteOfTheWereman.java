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

import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;

/**
 * Rite of the Wereman: the werewolf post-10 upgrade. Performed at werewolf level 10 while
 * shifted into the wolfman form; grants the Wereman upgrade (+1 level), letting the player
 * hold items and wear armor in wolfman form while keeping their powers.
 */
public class RiteOfTheWereman extends Rite {

    @Override
    public void addSteps(ArrayList steps, int initialStage) {
        steps.add(new StepWereman(initialStage));
    }

    private static class StepWereman extends RitualStep {

        StepWereman(int initialStage) {
            super(true);
        }

        @Override
        public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
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
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.rite.wereman.refused", new Object[0]);
                return RitualStep.Result.ABORTED_REFUND;
            }
            WitcheryUpgradeHelper we = (WitcheryUpgradeHelper) ex;
            if (we.witcheryExtras$isWereman()) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.rite.wereman.already", new Object[0]);
                return RitualStep.Result.COMPLETED;
            }
            if (ex.getWerewolfLevel() < 10) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.rite.wereman.notready", new Object[0]);
                return RitualStep.Result.ABORTED_REFUND;
            }
            if (player.getEntityData().getBoolean("WitcheryExtrasWeremanPending")) {
                // second stage: transformation witnessed, complete the rite
                we.witcheryExtras$setWereman(true);
                ex.setWerewolfLevel(ex.getWerewolfLevel() + 1);
                player.getEntityData().setBoolean("WitcheryExtrasWeremanPending", false);
                ChatUtil.sendTranslated(EnumChatFormatting.LIGHT_PURPLE, player, "witcheryextras.rite.wereman.completed", new Object[0]);
                ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_LEVELUP, player, 1.0D, 2.0D, 64);
                return RitualStep.Result.COMPLETED;
            }
            // first stage: require the shift into wolfman form within the circle
            if (ex.getCreatureType() != com.emoniph.witchery.util.TransformCreature.WOLFMAN) {
                ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.rite.wereman.shift", new Object[0]);
                return RitualStep.Result.ABORTED_REFUND;
            }
            player.getEntityData().setBoolean("WitcheryExtrasWeremanPending", true);
            ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.rite.wereman.stage2", new Object[0]);
            return RitualStep.Result.STARTING;
        }
    }
}
