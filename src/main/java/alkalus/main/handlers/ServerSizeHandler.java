package alkalus.main.handlers;

import static alkalus.main.core.WitcheryExtras.NETWORK;
import static alkalus.main.mixins.hooks.EntitySizeManager.OFFSET_PROPERTY;
import static alkalus.main.mixins.hooks.EntitySizeManager.getTargetYOffset;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.IExtendedEntityProperties;

import com.emoniph.witchery.common.Shapeshift;

import alkalus.main.mixins.hooks.EntitySizeManager;
import alkalus.main.network.EntitySizeSyncPacket;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ServerSizeHandler {

    private static final int RESYNC_DELAY_TICKS = 10;

    private final java.util.Map<UUID, Integer> pendingResync = new java.util.HashMap<>();

    @SubscribeEvent
    public void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.player.worldObj.isRemote) {
            pendingResync.put(event.player.getUniqueID(), RESYNC_DELAY_TICKS);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        pendingResync.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onUpdateServer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof EntityPlayerMP player)) {
            return;
        }
        float tmp = getTargetYOffset(player);
        IExtendedEntityProperties prop = player.getExtendedProperties(OFFSET_PROPERTY);
        if (prop instanceof EntitySizeManager.OffsetContents contents) {
            if (contents.targetOffset != tmp) {
                contents.targetOffset = tmp;
                NETWORK.sendTo(new EntitySizeSyncPacket(tmp), player);
            }
        }

        Integer delay = pendingResync.get(player.getUniqueID());
        if (delay != null) {
            if (delay <= 0) {
                pendingResync.remove(player.getUniqueID());
                if (Shapeshift.INSTANCE.isAnimalForm(player)) {
                    Shapeshift.INSTANCE.initCurrentShift(player);
                }
            } else {
                pendingResync.put(player.getUniqueID(), delay - 1);
            }
        }
    }
}
