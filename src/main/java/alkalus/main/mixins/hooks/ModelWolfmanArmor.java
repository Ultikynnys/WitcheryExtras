package alkalus.main.mixins.hooks;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import com.emoniph.witchery.client.model.ModelWolfman;

/**
 * Beast-form armor shells, generated from the Beast Armor Editor project configuration. Boxes are placed at exactly the
 * editor's position/size and sample the editor's per-face UV layout, so the in-game geometry and texture mapping are
 * 1:1 with the editor preview. Rotated shells rotate about their own centre; all boxes are inflated outward so they
 * render above the wolf fur without z-fighting.
 */
public class ModelWolfmanArmor extends ModelWolfman {

    private final ModelRenderer skullShell;
    private final ModelRenderer earShellL;
    private final ModelRenderer earShellR;
    private final ModelRenderer snoutShell;
    private final ModelRenderer Head;

    private final ModelRenderer TorsoUpper;
    private final ModelRenderer TorsoLower;
    private final ModelRenderer armShellR;
    private final ModelRenderer armShellL;

    private final ModelRenderer thighShellR;
    private final ModelRenderer thighShellL;
    private final ModelRenderer footShellR;
    private final ModelRenderer footShellL;
    private final ModelRenderer tailBaseShell;

    private boolean faceShells;

    private static final float INFLATE = 0.2F;

    public ModelWolfmanArmor(float scale) {
        super(scale);
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.headMain = this.part(4, 4);
        this.headMain.setRotationPoint(0.0F, 0.0F, -2.0F);
        this.skullShell = this.addChild(
                this.headMain,
                4,
                4,
                -3.0F,
                -6.0F,
                -2.0F,
                6,
                6,
                4,
                new int[] { 8, 8, 8, 9, 24, 8, 8, 8, 0, 8, 8, 8, 16, 8, 8, 8, 8, 4, 8, 4, 14, 4, 6, 4 });
        this.earShellL = this.addChild(
                this.headMain,
                16,
                20,
                -3.0F,
                -8.0F,
                1.0F,
                2,
                2,
                1,
                new int[] { 12, 20, 3, 4, 13, 21, 2, 2, 14, 21, 1, 2, 14, 21, 1, 2, 13, 21, 2, 1, 13, 21, 2, 1 });
        this.earShellR = this.addMirrored(
                this.headMain,
                16,
                20,
                1.0F,
                -8.0F,
                1.0F,
                2,
                2,
                1,
                new int[] { 12, 20, 3, 4, 13, 21, 2, 2, 14, 21, 1, 2, 14, 21, 1, 2, 13, 21, 2, 1, 13, 21, 2, 1 });
        this.snoutShell = this.addChild(
                this.headMain,
                16,
                8,
                -1.5F,
                -3.1F,
                -5.0F,
                3,
                3,
                4,
                new int[] { 12, 19, 3, 2, 12, 19, 3, 3, 12, 20, 3, 2, 12, 19, 3, 2, 12, 18, 3, 4, 11, 19, 3, 4 });
        this.Head = this.addChild(
                this.headMain,
                4,
                4,
                -3.0F,
                -2.9F,
                -2.15F,
                6,
                3,
                4,
                new int[] { 20, 16, 8, 4, -1, 1, 6, 3, 3, 1, 4, 3, 2, 2, 4, 3, 1, 1, 5, 3, 0, 17, 6, 4 });

        this.bodyUpper = this.part(16, 20);
        this.bodyUpper.setRotationPoint(0.0F, -0.1F, -2.0F);
        this.setRotateAngle(this.bodyUpper, 0.4098033F, 0.0F, 0.0F);
        this.TorsoUpper = this.addChild(
                this.bodyUpper,
                16,
                20,
                -5.5F,
                -0.46F,
                -4.49F,
                11,
                8,
                9,
                new int[] { 20, 22, 8, 5, 32, 20, 8, 5, 27, 19, 5, 9, 15, 19, 5, 9, 20, 20, 8, 3, 32, 25, 8, 4 });
        this.TorsoLower = this.addChildCentred(
                this.bodyUpper,
                11,
                13,
                -4.64F,
                5.24F,
                -4.24F,
                9,
                9,
                7,
                -0.05358208587129809F,
                0.0F,
                0.0F,
                new int[] { 21, 26, 6, 5, 31, 23, 6, 6, 30, 18, 7, 9, 31, 20, 7, 9, 18, 13, 9, 7, 29, 26, 7, 3 });

        this.armRight = this.part(40, 16);
        this.armRight.setRotationPoint(-5.8F, 2.0F, 0.0F);
        this.armShellR = this.addMirroredCentred(
                this.armRight,
                40,
                16,
                -3.48F,
                -2.26F,
                -2.5F,
                5,
                15,
                5,
                0.0F,
                1.5707964F,
                0.0F,
                new int[] { 47, 20, 5, 12, 47, 20, 5, 12, 51, 20, 5, 12, 51, 20, 5, 12, 44, 16, 4, 5, 47, 30, 3, 2 });

        this.armLeft = this.part(40, 16);
        this.armLeft.setRotationPoint(6.0F, 2.0F, 0.0F);
        this.armShellL = this.addMirroredCentred(
                this.armLeft,
                40,
                16,
                -1.52F,
                -2.26F,
                -2.5F,
                5,
                15,
                5,
                0.0F,
                -1.5707964F,
                0.0F,
                new int[] { 47, 20, 5, 12, 47, 20, 5, 12, 51, 20, 5, 12, 51, 20, 5, 12, 44, 16, 4, 5, 47, 30, 3, 2 });

        this.legRightUpper = this.part(16, 20);
        this.legRightUpper.setRotationPoint(-2.0F, 12.0F, 0.0F);
        this.setRotateAngle(this.legRightUpper, -0.4098033F, 0.0F, 0.0F);
        this.thighShellR = this.addChild(
                this.legRightUpper,
                0,
                20,
                -2.0F,
                1.06F,
                -2.0F,
                4,
                6,
                4,
                new int[] { 4, 23, 4, 6, 0, 22, 4, 6, 0, 22, 4, 6, 0, 22, 4, 6, 4, 20, 4, 4, 8, 20, 4, 4 });
        this.footShellR = this.addMirrored(
                this.legRightUpper,
                16,
                20,
                -2.0F,
                3.5F,
                2.0F,
                4,
                8,
                4,
                new int[] { 4, 26, 4, 6, 12, 26, 4, 6, 0, 26, 4, 6, 8, 26, 4, 6, 8, 16, 4, 4, 8, 16, 4, 4 });

        this.legLeftUpper = this.part(16, 20);
        this.legLeftUpper.setRotationPoint(2.0F, 12.0F, 0.0F);
        this.setRotateAngle(this.legLeftUpper, -0.4098033F, 0.0F, 0.0F);
        this.thighShellL = this.addMirrored(
                this.legLeftUpper,
                0,
                20,
                -2.0F,
                1.06F,
                -2.0F,
                4,
                6,
                4,
                new int[] { 4, 23, 4, 6, 0, 22, 4, 6, 0, 22, 4, 6, 0, 22, 4, 6, 4, 20, 4, 4, 8, 20, 4, 4 });
        this.footShellL = this.addChild(
                this.legLeftUpper,
                16,
                20,
                -2.0F,
                3.5F,
                2.0F,
                4,
                8,
                4,
                new int[] { 4, 26, 4, 6, 12, 26, 4, 6, 0, 26, 4, 6, 8, 26, 4, 6, 8, 16, 4, 4, 8, 16, 4, 4 });

        this.tail = this.part(16, 20);
        this.tail.setRotationPoint(0.0F, 11.9F, 3.6F);
        this.setRotateAngle(this.tail, 0.59184116F, 0.0F, 0.0F);
        this.tailBaseShell = this.addChild(
                this.tail,
                0,
                20,
                -1.56F,
                -0.62F,
                -1.5F,
                3,
                11,
                3,
                new int[] { 16, 19, 3, 8, 16, 18, 3, 9, 16, 18, 3, 8, 16, 18, 3, 8, 16, 19, 3, 3, 16, 18, 4, 4 });
    }

    private ModelRenderer part(int u, int v) {
        return new ModelRenderer(this, u, v);
    }

    public void setFaceShells(boolean on) {
        this.faceShells = on;
    }

    private ModelRenderer addChild(ModelRenderer parent, int u, int v, float x, float y, float z, int w, int h, int d,
            int[] faceUV) {
        return this.make(parent, u, v, false, x, y, z, w, h, d, 0.0F, 0.0F, 0.0F, faceUV);
    }

    private ModelRenderer addMirrored(ModelRenderer parent, int u, int v, float x, float y, float z, int w, int h,
            int d, int[] faceUV) {
        return this.make(parent, u, v, true, x, y, z, w, h, d, 0.0F, 0.0F, 0.0F, faceUV);
    }

    private ModelRenderer addChildCentred(ModelRenderer parent, int u, int v, float x, float y, float z, int w, int h,
            int d, float rx, float ry, float rz, int[] faceUV) {
        return this.make(parent, u, v, false, x, y, z, w, h, d, rx, ry, rz, faceUV);
    }

    private ModelRenderer addMirroredCentred(ModelRenderer parent, int u, int v, float x, float y, float z, int w,
            int h, int d, float rx, float ry, float rz, int[] faceUV) {
        return this.make(parent, u, v, true, x, y, z, w, h, d, rx, ry, rz, faceUV);
    }

    private ModelRenderer make(ModelRenderer parent, int u, int v, boolean mirror, float x, float y, float z, int w,
            int h, int d, float rx, float ry, float rz, int[] faceUV) {
        float ix = x - INFLATE;
        float iy = y - INFLATE;
        float iz = z - INFLATE;
        float iw = w + 2.0F * INFLATE;
        float ih = h + 2.0F * INFLATE;
        float id = d + 2.0F * INFLATE;
        ModelRenderer part = new ModelRenderer(this, u, v);
        if (rx != 0.0F || ry != 0.0F || rz != 0.0F) {
            part.setRotationPoint(ix + iw / 2.0F, iy + ih / 2.0F, iz + id / 2.0F);
            part.rotateAngleX = rx;
            part.rotateAngleY = ry;
            part.rotateAngleZ = rz;
            part.cubeList.add(new ModelBoxFaceUV(part, -iw / 2.0F, -ih / 2.0F, -id / 2.0F, iw, ih, id, faceUV, mirror));
        } else {
            part.cubeList.add(new ModelBoxFaceUV(part, ix, iy, iz, iw, ih, id, faceUV, mirror));
        }
        parent.addChild(part);
        return part;
    }

    public void setArmorSlot(int slot) {
        boolean helmet = slot == 0;
        boolean chest = slot == 1;
        boolean legs = slot == 2;
        boolean boots = slot == 3;
        this.skullShell.showModel = helmet;
        this.earShellL.showModel = helmet;
        this.earShellR.showModel = helmet;
        this.snoutShell.showModel = helmet && this.faceShells;
        this.Head.showModel = helmet;
        this.TorsoUpper.showModel = chest;
        this.TorsoLower.showModel = chest;
        this.armShellR.showModel = chest;
        this.armShellL.showModel = chest;
        this.thighShellR.showModel = legs;
        this.thighShellL.showModel = legs;
        this.tailBaseShell.showModel = legs;
        this.footShellR.showModel = boots;
        this.footShellL.showModel = boots;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw,
            float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
        this.headMain.render(scale);
        this.bodyUpper.render(scale);
        this.armRight.render(scale);
        this.armLeft.render(scale);
        this.legRightUpper.render(scale);
        this.legLeftUpper.render(scale);
        this.tail.render(scale);
    }
}
