package alkalus.main.handlers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.ritual.RiteRegistry;
import com.emoniph.witchery.ritual.Sacrifice;
import com.emoniph.witchery.ritual.SacrificeItem;
import com.emoniph.witchery.ritual.SacrificeMultiple;
import com.emoniph.witchery.ritual.SacrificePower;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
 * Registers the Rite of the Wereman into RiteRegistry. Runs in postInit so Witchery's own rites (IDs 1-96) exist first;
 * ours takes ID 100 and bookIndex 50 (end of the book). The Circle Magic book renders every registered rite
 * automatically.
 */
public class WeremanRiteRegistrar {

    public static final WeremanRiteRegistrar INSTANCE = new WeremanRiteRegistrar();

    private static final int RITUAL_ID = 100;
    private static final int BOOK_INDEX = 50;

    public void register(FMLPostInitializationEvent event) {
        RiteRegistry.addRecipe(
                RITUAL_ID,
                BOOK_INDEX,
                new RiteOfTheWereman(),
                new SacrificeMultiple(
                        new Sacrifice[] {
                                new SacrificeItem(
                                        new ItemStack[] { new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1),
                                                Witchery.Items.GENERIC.itemWolfsbane.createStack(),
                                                new ItemStack(Items.iron_ingot),
                                                Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack() }),
                                new SacrificePower(3000.0F, 20) }),
                java.util.EnumSet.of(com.emoniph.witchery.ritual.RitualTraits.ONLY_AT_NIGHT),
                new com.emoniph.witchery.ritual.Circle(0, 0, 40),
                new com.emoniph.witchery.ritual.Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.wereman");
    }
}
