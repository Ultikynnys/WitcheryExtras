package alkalus.main.mixins.hooks;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public final class TailAnimationHelper {

    public static final String VELOCITY_TAIL_FLAG = "witcheryextras$velocityTail";

    public static final float BASE_ANGLE = 15.0F * (float) (Math.PI / 180.0);
    public static final float MAX_ANGLE = (float) (Math.PI / 2.0);
    public static final float WOLF_SNEAK_ANGLE = 1.95F;
    public static final float BEAST_SNEAK_ANGLE = 70.0F * (float) (Math.PI / 180.0);

    private TailAnimationHelper() {}

    public static float computeVelocityPitch(Entity entity, float limbSwingAmount) {
        if (entity != null && entity.isRiding()) {
            return BASE_ANGLE;
        }
        float motionSpeed = entity != null
                ? MathHelper.sqrt_double(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ)
                : 0.0F;
        float progress = Math.max(motionSpeed / 0.35F, limbSwingAmount);
        return MathHelper.clamp_float(BASE_ANGLE + (MAX_ANGLE - BASE_ANGLE) * progress, BASE_ANGLE, MAX_ANGLE);
    }

    public static float computeWolfTailPitch(Entity entity, float limbSwingAmount, boolean isSneaking) {
        if (isSneaking) {
            return WOLF_SNEAK_ANGLE;
        }
        return computeVelocityPitch(entity, limbSwingAmount);
    }

    public static float computeBeastTailPitch(Entity entity, float limbSwingAmount, boolean isSneaking) {
        if (isSneaking) {
            return BEAST_SNEAK_ANGLE;
        }
        return computeVelocityPitch(entity, limbSwingAmount);
    }
}
