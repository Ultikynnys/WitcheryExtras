package alkalus.main.mixins.late.witchery;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.client.model.ModelWolfman;

import alkalus.main.mixins.hooks.TailAnimationHelper;

/**
 * Wolfman tail follows the hips and animates cleanly: when sneaking, raises up behind the back and tracks the shifted
 * pelvis; when walking or running, shares the unified velocity-driven pitch with wolf form.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ModelWolfman.class)
public abstract class ModelWolfmanTailMixin {

    @Shadow(remap = false)
    public ModelRenderer tail;

    @Shadow(remap = false)
    public boolean isSneak;

    @Inject(method = { "setRotationAngles", "func_78087_a" }, at = @At("TAIL"), remap = false)
    private void witcheryextras$raiseTailWhenSneaking(float swing, float amount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale, Entity entity, CallbackInfo ci) {
        boolean isCrouching = this.isSneak || (entity != null && entity.isSneaking());
        if (isCrouching) {
            this.tail.rotationPointY = 8.5F;
            this.tail.rotationPointZ = 6.0F;
            this.tail.rotateAngleY = 0.0F;
        } else {
            this.tail.rotationPointY = 11.9F;
            this.tail.rotationPointZ = 3.6F;
        }
        this.tail.rotateAngleX = TailAnimationHelper.computeBeastTailPitch(entity, amount, isCrouching);
    }
}
