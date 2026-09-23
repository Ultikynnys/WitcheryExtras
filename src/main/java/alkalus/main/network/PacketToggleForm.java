package alkalus.main.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.item.ItemMoonCharm;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TransformCreature;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketToggleForm implements IMessage {

    public PacketToggleForm() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketToggleForm, IMessage> {

        @Override
        public IMessage onMessage(PacketToggleForm message, MessageContext ctx) {
            EntityPlayer player = Witchery.proxy.getPlayer(ctx);
            if (player == null) {
                return null;
            }
            ItemStack charm = findCharm(player);
            if (charm == null) {
                return null;
            }
            ExtendedPlayer playerEx = ExtendedPlayer.get(player);
            if (playerEx == null || ItemMoonCharm.isWolfsbaneActive(player, playerEx)
                    || !Shapeshift.INSTANCE.canControlTransform(playerEx)) {
                return null;
            }
            TransformCreature target = nextCreature(player, playerEx);
            if (target == null) {
                return null;
            }
            Shapeshift.INSTANCE.shiftTo(player, target);
            ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
            charm.damageItem(1, player);
            return null;
        }

        private static TransformCreature nextCreature(EntityPlayer player, ExtendedPlayer playerEx) {
            boolean sneak = player.isSneaking();
            boolean wolfmanAllowed = Shapeshift.INSTANCE.isWolfmanAllowed(playerEx);
            switch (playerEx.getCreatureType()) {
                case NONE:
                    return sneak && wolfmanAllowed ? TransformCreature.WOLFMAN : TransformCreature.WOLF;
                case WOLF:
                    return sneak && wolfmanAllowed ? TransformCreature.WOLFMAN : TransformCreature.NONE;
                case WOLFMAN:
                    return sneak ? TransformCreature.NONE : TransformCreature.WOLF;
                default:
                    return null;
            }
        }

        private static ItemStack findCharm(EntityPlayer player) {
            ItemStack held = player.getHeldItem();
            if (isCharm(held)) {
                return held;
            }
            IInventory baubles = baublesOf(player);
            if (baubles != null) {
                for (int slot = 0; slot < baubles.getSizeInventory(); slot++) {
                    ItemStack stack = baubles.getStackInSlot(slot);
                    if (isCharm(stack)) {
                        return stack;
                    }
                }
            }
            for (ItemStack stack : player.inventory.mainInventory) {
                if (isCharm(stack)) {
                    return stack;
                }
            }
            return null;
        }

        private static IInventory baublesOf(EntityPlayer player) {
            if (!Loader.isModLoaded("Baubles") && !Loader.isModLoaded("Baubles|Expanded")) {
                return null;
            }
            try {
                Class<?> api = Class.forName("baubles.api.BaublesApi");
                Object inventory = api.getMethod("getBaubles", EntityPlayer.class).invoke(null, player);
                return inventory instanceof IInventory ? (IInventory) inventory : null;
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }

        private static boolean isCharm(ItemStack stack) {
            return stack != null && stack.getItem() == Witchery.Items.MOON_CHARM;
        }
    }
}
