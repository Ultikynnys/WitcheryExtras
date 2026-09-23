package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.GenericEvents;
import com.emoniph.witchery.util.TransformCreature;

import alkalus.main.core.WitcheryUpgrades;

/**
 * Stock onPlayerSleepInBed blocks sleeping for anyone who is a werewolf in wolf or werewolf form. A werewolf with Form
 * Mastery may sleep while in werewolf form; the wolf form restriction is left untouched (creature type stays WOLF, so
 * this handler returns early and the stock block still applies).
 */
@SuppressWarnings("UnusedMixin")
@Mixin(GenericEvents.class)
public abstract class GenericEventsSleepMixin {

    @Inject(method = "onPlayerSleepInBed", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$allowMasteredSleep(PlayerSleepInBedEvent event, CallbackInfo ci) {
        EntityPlayer player = event.entityPlayer;
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || ex.getCreatureType() != TransformCreature.WOLFMAN) {
            return;
        }
        if (WitcheryUpgrades.hasFormMastery(player)) {
            ci.cancel();
        }
    }
}
