package alkalus.main.mixins.late.witchery;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.emoniph.witchery.common.ExtendedPlayer;

/**
 * Raises the vampire level ceiling from 10 to 11 ("Twilight Vampire").
 * <ul>
 * <li>{@code setVampireLevel} hard-codes {@code level <= 10} - raised to 11.</li>
 * <li>{@code loadNBTData} clamps the stored level to {@code 0..10} - raised to 0..11.</li>
 * </ul>
 * Array indexing by vampire level elsewhere is handled by {@link ExtendedPlayerVampirePowerMixin} and
 * {@link ShapeshiftMixin}.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ExtendedPlayer.class)
public abstract class ExtendedPlayerLevelMixin {

    @ModifyConstant(method = "setVampireLevel", constant = @Constant(intValue = 10), require = 1, remap = false)
    private int witcheryextras$raiseSetLevelCap(int original) {
        return 11;
    }

    @ModifyConstant(method = "loadNBTData", constant = @Constant(intValue = 10, ordinal = -1), require = 0, remap = false)
    private int witcheryextras$raiseNbtClamp(int original) {
        return 11;
    }
}
