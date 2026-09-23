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
 * Registers the werewolf ascension rites into RiteRegistry. Runs in postInit so Witchery's own rites (IDs 1-96) exist
 * first; ours take IDs 100 and 101 and bookIndexes 50 and 51 (end of the book). The Circle Magic book renders every
 * registered rite automatically: the first grants Greater Form Control, the second Form Mastery.
 */
public class WeremanRiteRegistrar {

    public static final WeremanRiteRegistrar INSTANCE = new WeremanRiteRegistrar();

    public void register(FMLPostInitializationEvent event) {
        RiteRegistry
                .addRecipe(
                        100,
                        50,
                        new WerewolfProgressionRite("greaterformcontrol", 10, false),
                        sacrifice(),
                        java.util.EnumSet.of(com.emoniph.witchery.ritual.RitualTraits.ONLY_AT_NIGHT),
                        new com.emoniph.witchery.ritual.Circle(28, 0, 0),
                        new com.emoniph.witchery.ritual.Circle(40, 0, 0))
                .setUnlocalizedName("witchery.rite.greaterformcontrol");
        RiteRegistry
                .addRecipe(
                        101,
                        51,
                        new WerewolfProgressionRite("formmastery", 11, true),
                        sacrifice(),
                        java.util.EnumSet.of(com.emoniph.witchery.ritual.RitualTraits.ONLY_AT_NIGHT),
                        new com.emoniph.witchery.ritual.Circle(16, 0, 0),
                        new com.emoniph.witchery.ritual.Circle(40, 0, 0))
                .setUnlocalizedName("witchery.rite.formmastery");
    }

    private static SacrificeMultiple sacrifice() {
        return new SacrificeMultiple(
                new Sacrifice[] { new SacrificeItem(
                        new ItemStack[] { new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1),
                                Witchery.Items.GENERIC.itemWolfsbane.createStack(), new ItemStack(Items.iron_ingot),
                                Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack() }),
                        new SacrificePower(3000.0F, 20) });
    }
}
