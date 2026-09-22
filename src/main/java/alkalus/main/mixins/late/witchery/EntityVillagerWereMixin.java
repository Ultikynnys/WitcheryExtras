package alkalus.main.mixins.late.witchery;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.entity.EntityVillagerWere;

/**
 * Makes naturally-occurring were-villagers infectious, so their wolfman form can spread lycanthropy to players and
 * other villagers on a full moon. In stock Witchery 0.24.1 this flag is only granted via the silvered beartrap ritual;
 * werewolves converted from regular villagers (natural worldgen) always spawn non-infectious and can never pass the
 * curse on.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(EntityVillagerWere.class)
public class EntityVillagerWereMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/World;IZ)V", at = @At("TAIL"), remap = false)
    private void witcheryextras$makeInfectious(CallbackInfo ci) {
        try {
            Field f = EntityVillagerWere.class.getDeclaredField("infectious");
            f.setAccessible(true);
            f.setBoolean(this, true);
        } catch (ReflectiveOperationException ignored) {}
    }
}
