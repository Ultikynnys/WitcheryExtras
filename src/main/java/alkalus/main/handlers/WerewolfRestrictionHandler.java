package alkalus.main.handlers;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.TransformCreature;

import alkalus.main.core.WitcheryUpgradeHelper;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Instant enforcement of the werewolf item restrictions. Stock Witchery only sweeps armor and the held item every 40
 * ticks (GenericEvents.updateWerewolfEffects via onLivingUpdate), so a freshly equipped piece sits on the player for up
 * to two seconds and drops come back with no pickup delay.
 * <ul>
 * <li>Armor: dropped the tick after it is equipped, with a pickup delay.</li>
 * <li>Held item: dropped the tick after it lands in the active hotbar slot. For a wolf that slot is always empty, so a
 * pickup would always land there - the pickup is canceled outright instead. The item never enters the inventory, so
 * there is no drop/pickup loop and no lag machine.</li>
 * <li>Wereman upgrade holders are exempt from both: the point of the upgrade is holding items and wearing armor in
 * beast form.</li>
 * </ul>
 */
public class WerewolfRestrictionHandler {

    private static final int MESSAGE_THROTTLE_TICKS = 40;
    private static final int PICKUP_DELAY_TICKS = 40;

    private final java.util.Map<String, Integer> nextMessageTick = new java.util.HashMap<>();

    public boolean isRestricted(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || player.capabilities.isCreativeMode) {
            return false;
        }
        if (ex.getCreatureType() != TransformCreature.WOLF) {
            return false;
        }
        return !(ex.getWerewolfLevel() >= 11 && ((WitcheryUpgradeHelper) ex).witcheryExtras$isWereman());
    }

    /** Wolves never pick up items: anything picked up would land in the always-empty active slot. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityItemPickupEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.worldObj.isRemote || event.isCanceled() || !isRestricted(player)) {
            return;
        }
        ItemStack pickedUp = event.item.getEntityItem();
        if (pickedUp == null || player.inventory.getStackInSlot(player.inventory.currentItem) != null) {
            return;
        }
        event.setCanceled(true);
        sendMessage(player);
    }

    /** Drops anything that slipped in through a GUI, the same tick, with a pickup delay. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.player;
        if (!isRestricted(player)) {
            return;
        }
        for (int slot = 1; slot <= 4; slot++) {
            ItemStack armor = player.getEquipmentInSlot(slot);
            if (armor != null) {
                dropRestricted(player, armor, slot);
            }
        }
        ItemStack held = player.getHeldItem();
        if (held != null) {
            dropRestricted(player, held, 0);
        }
    }

    private void dropRestricted(EntityPlayer player, ItemStack stack, int slot) {
        player.setCurrentItemOrArmor(slot, null);
        EntityItem dropped = player.entityDropItem(stack, 1.0F);
        if (dropped != null) {
            dropped.delayBeforeCanPickup = PICKUP_DELAY_TICKS;
        }
        sendMessage(player);
    }

    private void sendMessage(EntityPlayer player) {
        Integer next = nextMessageTick.get(player.getCommandSenderName());
        if (next != null && player.ticksExisted < next) {
            return;
        }
        nextMessageTick.put(player.getCommandSenderName(), player.ticksExisted + MESSAGE_THROTTLE_TICKS);
        ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witcheryextras.wolf.restricted", new Object[0]);
    }

}
