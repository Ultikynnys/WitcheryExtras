package alkalus.main.mixins.late.witchery;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.item.ItemMoonCharm;

import baubles.api.BaubleType;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.BaubleItemHelper;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mixin(ItemMoonCharm.class)
public abstract class ItemMoonCharmMixin_Bauble implements IBaubleExpanded {

    @Unique
    private static final String[] witcheryExtras$moonCharmBaubleTypes = { BaubleExpandedSlots.charmType };

    @SideOnly(Side.CLIENT)
    @Inject(method = "addInformation", at = @At(value = "TAIL"))
    public void witcheryExtras$addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip,
            boolean expanded, CallbackInfo ci) {
        BaubleItemHelper.addSlotInformation(tooltip, witcheryExtras$moonCharmBaubleTypes);
    }

    // Baubles interface methods
    @Unique
    @Override
    public String[] getBaubleTypes(ItemStack itemstack) {
        return witcheryExtras$moonCharmBaubleTypes;
    }

    @Unique
    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return null;
    }

    @Unique
    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {}

    @Unique
    @Override
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

    @Unique
    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

    @Unique
    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Unique
    @Override
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }
}
