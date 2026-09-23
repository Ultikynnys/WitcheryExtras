package alkalus.main.mixins.late.witchery;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.client.model.ModelWolfman;

/**
 * Stock keeps the wolfman tail angled back and down (rotateAngleX 0.59) even while crouched, so it dips through the
 * legs when sneaking. Raise it to point up whenever the model is sneaking.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ModelWolfman.class)
public abstract class ModelWolfmanTailMixin {

    @Shadow(remap = false)
    public ModelRenderer tail;

    @Shadow(remap = false)
    public boolean isSneak;

    @Inject(method = "setRotationAngles", at = @At("TAIL"), remap = false)
    private void witcheryextras$raiseTailWhenSneaking(float swing, float amount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale, Entity entity, CallbackInfo ci) {
        if (this.isSneak) {
            this.tail.rotateAngleX = -0.4F;
        }
    }
}
