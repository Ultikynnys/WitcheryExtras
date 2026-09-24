package alkalus.main.mixins.late.witchery;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emoniph.witchery.client.TransformWolf;

import alkalus.main.mixins.hooks.TailAnimationHelper;

/**
 * Vanilla RenderWolf never renders a held item (RendererLivingEntity.renderEquippedItems is empty and RenderWolf does
 * not override it), so a wolf-form werewolf's item is invisible in third person even though the server keeps it. This
 * draws the held item anchored at the wolf's muzzle, mirroring how the beast carries an item in its jaws.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(TransformWolf.class)
public abstract class TransformWolfMixin {

    @Shadow(remap = false)
    private EntityWolf proxyEntity;

    @Inject(method = "syncModelWith", at = @At("TAIL"), remap = false)
    private void witcheryextras$flagVelocityTail(EntityLivingBase entity, boolean frontface, CallbackInfo ci) {
        if (this.proxyEntity != null) {
            this.proxyEntity.getEntityData().setBoolean(TailAnimationHelper.VELOCITY_TAIL_FLAG, true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void witcheryextras$renderMuzzleItem(World worldObj, EntityLivingBase entity, double x, double y, double z,
            RendererLivingEntity renderer, float partialTicks, boolean frontface, CallbackInfo ci) {
        if (this.proxyEntity == null) {
            return;
        }
        ItemStack held = entity.getHeldItem();
        if (held == null || held.getItem() == null) {
            return;
        }
        float bodyYaw;
        float headYaw;
        float headPitch;
        if (frontface) {
            bodyYaw = 0.0F;
            headYaw = 0.0F;
            headPitch = 0.0F;
        } else {
            bodyYaw = witcheryextras$interp(
                    this.proxyEntity.prevRenderYawOffset,
                    this.proxyEntity.renderYawOffset,
                    partialTicks);
            headYaw = witcheryextras$interp(
                    this.proxyEntity.prevRotationYawHead,
                    this.proxyEntity.rotationYawHead,
                    partialTicks) - bodyYaw;
            headPitch = this.proxyEntity.prevRotationPitch
                    + (this.proxyEntity.rotationPitch - this.proxyEntity.prevRotationPitch) * partialTicks;
        }
        double d3 = -((double) this.proxyEntity.yOffset);
        if (this.proxyEntity.isSneaking() && !(entity instanceof EntityPlayerSP)) {
            d3 -= 0.125D;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x, y + d3, z);
        GL11.glRotatef(180.0F - bodyYaw, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-1.0F, -1.0F, 1.0F);
        GL11.glTranslatef(0.0F, -1.5078125F, 0.0F);
        GL11.glTranslatef(-0.0625F, 0.84375F, -0.4375F);
        GL11.glRotatef(headYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(headPitch, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, 0.09375F, -0.21875F);
        this.witcheryextras$drawHeldItem(entity, held);
        GL11.glPopMatrix();
    }

    private static float witcheryextras$interp(float prev, float current, float partialTicks) {
        float delta = current - prev;
        while (delta < -180.0F) {
            delta += 360.0F;
        }
        while (delta >= 180.0F) {
            delta -= 360.0F;
        }
        return prev + partialTicks * delta;
    }

    private void witcheryextras$drawHeldItem(EntityLivingBase entity, ItemStack itemstack) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Item item = itemstack.getItem();
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.EQUIPPED);
        boolean is3D = customRenderer != null && customRenderer
                .shouldUseRenderHelper(ItemRenderType.EQUIPPED, itemstack, ItemRendererHelper.BLOCK_3D);
        float f1;
        if (item instanceof ItemBlock
                && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).getRenderType()))) {
            f1 = 0.5F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            f1 *= 0.75F;
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-f1, -f1, f1);
        } else if (item == Items.bow) {
            f1 = 0.625F;
            GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
            GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(f1, -f1, f1);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        } else if (item.isFull3D()) {
            f1 = 0.625F;
            if (item.shouldRotateAroundWhenRendering()) {
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.0F, -0.125F, 0.0F);
            }
            GL11.glScalef(f1, -f1, f1);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        } else {
            f1 = 0.375F;
            GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
            GL11.glScalef(f1, f1, f1);
            GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
        }
        RenderManager.instance.itemRenderer.renderItem(entity, itemstack, 0);
    }
}
