package alkalus.main.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import alkalus.main.core.WitcheryExtras;
import alkalus.main.handlers.ClientSizeHandler;
import alkalus.main.network.PacketToggleForm;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding TRANSFORM_KEY = new KeyBinding(
            "key.witcheryextras.transform",
            Keyboard.KEY_G,
            "key.categories.witcheryextras");

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.registerKeyBinding(TRANSFORM_KEY);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        FMLCommonHandler.instance().bus().register(new ClientSizeHandler());
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().currentScreen != null) {
            return;
        }
        if (TRANSFORM_KEY.isPressed()) {
            WitcheryExtras.NETWORK.sendToServer(new PacketToggleForm());
        }
    }
}
