package alkalus.main.mixins.late.witchery;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.emoniph.witchery.client.model.ModelWolfman;
import com.emoniph.witchery.client.renderer.RenderWolfman;

import alkalus.main.mixins.hooks.ModelWolfmanArmor;

/**
 * Replaces the stock 64x64 entity-mapped armor models with ModelWolfmanArmor, which uses standard 64x32 biped armor UV
 * coordinates, tailored scaling, and per-slot visibility for the beast form. Also routes armor texture lookups through
 * Forge's RenderBiped.getArmorResource to correctly support modded armors (e.g. IC2 Nano Armor). The ears arm with any
 * helmet; the snout shell is added only for full-face helmets (nano/quantum by texture path) so open-face vanilla
 * helmets leave the muzzle bare.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(RenderWolfman.class)
public abstract class RenderWolfmanMixin {

    @Shadow(remap = false)
    protected ModelWolfman field_82423_g;

    @Shadow(remap = false)
    protected ModelWolfman field_82425_h;

    @Inject(method = "func_82421_b", at = @At("TAIL"), remap = false)
    private void witcheryextras$initArmorModels(CallbackInfo ci) {
        this.field_82423_g = new ModelWolfmanArmor(1.0F);
        this.field_82425_h = new ModelWolfmanArmor(0.5F);
    }

    @Inject(method = { "shouldRenderPass", "func_77032_a" }, at = @At("HEAD"), remap = false)
    private void witcheryextras$setArmorPassSlot(EntityLiving entity, int slot, float partialTicks,
            CallbackInfoReturnable<Integer> cir) {
        boolean faceShells = slot == 0 && RenderWolfmanMixin.isFullFaceHelmet(entity.getEquipmentInSlot(4));
        if (this.field_82423_g instanceof ModelWolfmanArmor) {
            ((ModelWolfmanArmor) this.field_82423_g).setFaceShells(faceShells);
            ((ModelWolfmanArmor) this.field_82423_g).setArmorSlot(slot);
        }
        if (this.field_82425_h instanceof ModelWolfmanArmor) {
            ((ModelWolfmanArmor) this.field_82425_h).setFaceShells(faceShells);
            ((ModelWolfmanArmor) this.field_82425_h).setArmorSlot(slot);
        }
    }

    private static boolean isFullFaceHelmet(ItemStack helmet) {
        if (helmet == null) {
            return false;
        }
        ResourceLocation tex = RenderBiped.getArmorResource(null, helmet, 3, null);
        String path = tex == null ? "" : tex.getResourcePath();
        return path.contains("nano") || path.contains("quantum");
    }

    /**
     * @author WitcheryExtras
     * @reason Fixes modded armors (e.g. IC2 Nano Armor, Quantum Armor, etc.): stock RenderWolfman referenced a
     *         hardcoded 5-element array (leather, chainmail, iron, diamond, gold) and crashed on modded renderIndex
     *         values. Delegate to Forge's RenderBiped.getArmorResource which properly resolves modded armor prefixes
     *         and ForgeHooksClient textures.
     */
    @Overwrite(remap = false)
    public static ResourceLocation getArmorResource(Entity entity, ItemStack stack, int slot, String type) {
        return RenderBiped.getArmorResource(entity, stack, slot, type);
    }
}
