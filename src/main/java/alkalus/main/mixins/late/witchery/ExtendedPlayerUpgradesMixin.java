package alkalus.main.mixins.late.witchery;

import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.emoniph.witchery.common.ExtendedPlayer;

import alkalus.main.core.WitcheryUpgradeHelper;

/**
 * Post-10 progression is upgrade-flag based: each earned upgrade (Twilight, Blood Magic, Greater Form Control, Form
 * Mastery) adds +1 to the effective level, in any order. The stock level field still holds the 0-10 quest ladder; flags
 * carry everything beyond it.
 * <ul>
 * <li>{@code setVampireLevel} cap 10 -&gt; 12, {@code setWerewolfLevel} cap 10 -&gt; 12 (Greater Form Control at 11,
 * Form Mastery at 12).</li>
 * <li>NBT load clamps raised to match, and the upgrade flags persisted alongside.</li>
 * </ul>
 */
@SuppressWarnings("UnusedMixin")
@Mixin(ExtendedPlayer.class)
public abstract class ExtendedPlayerUpgradesMixin implements WitcheryUpgradeHelper {

    @Unique
    private static final String WE_FLAGS_KEY = "WitcheryExtrasUpgrades";

    @Unique
    private boolean weTwilight;

    @Unique
    private boolean weBloodMagic;

    @Unique
    private boolean weGreaterFormControl;

    @Unique
    private boolean weFormMastery;

    @Override
    public boolean witcheryExtras$isTwilight() {
        return this.weTwilight;
    }

    @Override
    public boolean witcheryExtras$isBloodMagic() {
        return this.weBloodMagic;
    }

    @Override
    public boolean witcheryExtras$hasGreaterFormControl() {
        return this.weGreaterFormControl;
    }

    @Override
    public boolean witcheryExtras$hasFormMastery() {
        return this.weFormMastery;
    }

    @Override
    public void witcheryExtras$setTwilight(boolean value) {
        this.weTwilight = value;
    }

    @Override
    public void witcheryExtras$setBloodMagic(boolean value) {
        this.weBloodMagic = value;
    }

    @Override
    public void witcheryExtras$setGreaterFormControl(boolean value) {
        this.weGreaterFormControl = value;
    }

    @Override
    public void witcheryExtras$setFormMastery(boolean value) {
        this.weFormMastery = value;
    }

    @Shadow(remap = false)
    public abstract void saveNBTData(NBTTagCompound compound);

    @Inject(method = "saveNBTData", at = @At("TAIL"), remap = false)
    private void witcheryextras$saveFlags(NBTTagCompound compound, CallbackInfo ci) {
        NBTTagCompound we = new NBTTagCompound();
        we.setBoolean("Twilight", this.weTwilight);
        we.setBoolean("BloodMagic", this.weBloodMagic);
        we.setBoolean("GreaterFormControl", this.weGreaterFormControl);
        we.setBoolean("FormMastery", this.weFormMastery);
        compound.setTag(WE_FLAGS_KEY, we);
    }

    @Inject(method = "loadNBTData", at = @At("TAIL"), remap = false)
    private void witcheryextras$loadFlags(NBTTagCompound compound, CallbackInfo ci) {
        NBTTagCompound we = compound.getCompoundTag(WE_FLAGS_KEY);
        this.weTwilight = we.getBoolean("Twilight");
        this.weBloodMagic = we.getBoolean("BloodMagic");
        boolean legacyWereman = we.getBoolean("Wereman");
        this.weGreaterFormControl = we.getBoolean("GreaterFormControl") || legacyWereman;
        this.weFormMastery = we.getBoolean("FormMastery") || legacyWereman;
    }

    @Inject(method = "getMaxAvailablePowerOrdinal", at = @At("HEAD"), cancellable = true, remap = false)
    private void witcheryextras$clampMaxPowerOrdinal(CallbackInfoReturnable<Integer> cir) {
        if (((ExtendedPlayer) (Object) this).getVampireLevel() > 10) {
            cir.setReturnValue(ExtendedPlayer.VampirePower.ULTIMATE.ordinal());
        }
    }

    @ModifyConstant(method = "setVampireLevel", constant = @Constant(intValue = 10), require = 1, remap = false)
    private int witcheryextras$vampireCap(int original) {
        return 12;
    }

    @ModifyConstant(method = "setWerewolfLevel", constant = @Constant(intValue = 10), require = 1, remap = false)
    private int witcheryextras$werewolfCap(int original) {
        return 12;
    }

    @ModifyConstant(
            method = "loadNBTData",
            constant = @Constant(intValue = 10, ordinal = -1),
            require = 0,
            remap = false)
    private int witcheryextras$nbtClamps(int original) {
        return 12;
    }
}
