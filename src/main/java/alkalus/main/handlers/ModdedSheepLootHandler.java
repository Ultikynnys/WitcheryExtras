package alkalus.main.handlers;

import java.util.Locale;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.CreatureUtil;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Witchery's werewolf mutton-chop drop only fires for vanilla EntitySheep (an instanceof check plus a cast to
 * EntitySheep for the child check). This handler grants the same drop for any non-vanilla mob whose class name contains
 * "Sheep", mirroring the stock conditions: killer is a shifted werewolf, victim not a child, 3-in-4 chance.
 */
public class ModdedSheepLootHandler {

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.entityLiving.worldObj.isRemote) {
            return;
        }
        EntityLivingBase victim = event.entityLiving;
        if (victim instanceof EntitySheep) {
            return;
        }
        if (!victim.getClass().getSimpleName().toUpperCase(Locale.ROOT).contains("SHEEP")) {
            return;
        }
        if (victim.isChild() || victim.worldObj.rand.nextInt(4) == 0) {
            return;
        }
        if (!(event.source.getSourceOfDamage() instanceof EntityPlayer)) {
            return;
        }
        if (CreatureUtil.isWerewolf(event.source.getSourceOfDamage(), false)) {
            victim.entityDropItem(Witchery.Items.GENERIC.itemMuttonRaw.createStack(), 0.0F);
        }
    }
}
