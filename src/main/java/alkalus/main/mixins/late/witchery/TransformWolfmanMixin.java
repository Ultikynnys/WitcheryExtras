package alkalus.main.mixins.late.witchery;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.client.TransformWolfman;
import com.emoniph.witchery.entity.EntityWolfman;

import alkalus.main.core.WitcheryUpgrades;

/**
 * Copies the player's armor slots onto the wolfman render proxy. Stock code only copies the held item, so
 * RenderWolfman's per-section armor passes (head/chest/arms/legs) never fire and armor is invisible on transformed
 * players. A werewolf with the Form Mastery upgrade gets the four-part swap: helmet renders on the head, chestplate on
 * body+arms, leggings/boots on the wolf legs. Other players keep stock behavior.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(TransformWolfman.class)
public abstract class TransformWolfmanMixin {

    @Shadow(remap = false)
    private EntityWolfman proxyEntity;

    @Inject(method = "syncModelWith", at = @At("TAIL"), remap = false)
    private void witcheryextras$syncArmor(net.minecraft.entity.EntityLivingBase entity, boolean frontface,
            CallbackInfo ci) {
        if (!(entity instanceof EntityPlayer) || this.proxyEntity == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (!player.capabilities.isCreativeMode && !WitcheryUpgrades.hasFormMastery(player)) {
            return;
        }
        for (int slot = 1; slot <= 4; slot++) {
            this.proxyEntity.setCurrentItemOrArmor(slot, entity.getEquipmentInSlot(slot));
        }
    }
}
