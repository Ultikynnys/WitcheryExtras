package alkalus.main.mixins.late.minecraft;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import alkalus.main.mixins.hooks.TailAnimationHelper;

/**
 * Witchery's wolf form reuses the vanilla ModelWolf, whose tail wags side to side (rotateAngleY from limb swing) and
 * additionally rotates on X by raw ageInTicks. For the flagged proxy entity only, replace all of that with a tail that
 * hangs at rest, lifts toward horizontal as velocity increases, and tilts up cleanly when crouching.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ModelWolf.class)
public abstract class ModelWolfMixin {

    @Shadow
    public ModelRenderer wolfTail;

    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void witcheryextras$tailFromVelocity(float limbSwing, float limbSwingAmount, float ageInTicks,
            float headYaw, float headPitch, float scale, Entity entity, CallbackInfo ci) {
        if (entity == null || !entity.getEntityData().getBoolean(TailAnimationHelper.VELOCITY_TAIL_FLAG)) {
            return;
        }
        this.wolfTail.rotateAngleY = 0.0F;
        boolean isCrouching = entity.isSneaking();
        this.wolfTail.rotateAngleX = TailAnimationHelper.computeWolfTailPitch(entity, limbSwingAmount, isCrouching);
    }
}
