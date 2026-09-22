package alkalus.main.mixins.late.witchery;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.entity.EntityVillagerWere;

/**
 * Forces the infectious flag on were-villagers so natural wolfmen can spread lycanthropy, unlike stock Witchery.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(EntityVillagerWere.class)
public class EntityVillagerWereMixin {

    private static final Logger LOGGER = LogManager.getLogger("WitcheryExtras");

    @Inject(method = "<init>(Lnet/minecraft/world/World;IZ)V", at = @At("TAIL"), remap = false)
    private void witcheryextras$makeInfectious(CallbackInfo ci) {
        try {
            Field f = EntityVillagerWere.class.getDeclaredField("infectious");
            f.setAccessible(true);
            f.setBoolean(this, true);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("WitcheryExtras: failed to force infectious flag on were-villager", e);
        }
    }
}
