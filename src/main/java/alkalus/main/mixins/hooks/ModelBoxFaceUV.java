package alkalus.main.mixins.hooks;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A box whose six faces each sample an independent texel rectangle, so the editor's per-face UV layout (uvScale,
 * per-face offsets and per-face scales) is reproducible in-game. Face texture rectangles are given as 24 ints in the
 * order {north, south, west, east, top, bottom}, each {u, v, w, h}. Geometry is identical to vanilla ModelBox.
 */
public class ModelBoxFaceUV extends ModelBox {

    private final TexturedQuad[] quads = new TexturedQuad[6];

    public ModelBoxFaceUV(ModelRenderer renderer, float x, float y, float z, float w, float h, float d, int[] faceUV,
            boolean mirror) {
        super(renderer, 0, 0, x, y, z, Math.round(w), Math.round(h), Math.round(d), 0.0F);
        float x1 = x, y1 = y, z1 = z;
        float x2 = x + w, y2 = y + h, z2 = z + d;
        if (mirror) {
            float swap = x1;
            x1 = x2;
            x2 = swap;
        }
        PositionTextureVertex[] v = new PositionTextureVertex[] { new PositionTextureVertex(x1, y1, z1, 0.0F, 0.0F),
                new PositionTextureVertex(x2, y1, z1, 0.0F, 0.0F), new PositionTextureVertex(x2, y2, z1, 0.0F, 0.0F),
                new PositionTextureVertex(x1, y2, z1, 0.0F, 0.0F), new PositionTextureVertex(x1, y1, z2, 0.0F, 0.0F),
                new PositionTextureVertex(x2, y1, z2, 0.0F, 0.0F), new PositionTextureVertex(x2, y2, z2, 0.0F, 0.0F),
                new PositionTextureVertex(x1, y2, z2, 0.0F, 0.0F) };
        float texW = renderer.textureWidth, texH = renderer.textureHeight;
        this.quads[0] = this.quad(new PositionTextureVertex[] { v[5], v[1], v[2], v[6] }, faceUV, 12, texW, texH);
        this.quads[1] = this.quad(new PositionTextureVertex[] { v[0], v[4], v[7], v[3] }, faceUV, 8, texW, texH);
        this.quads[2] = this.quad(new PositionTextureVertex[] { v[5], v[4], v[0], v[1] }, faceUV, 16, texW, texH);
        this.quads[3] = this.quad(new PositionTextureVertex[] { v[2], v[3], v[7], v[6] }, faceUV, 20, texW, texH);
        this.quads[4] = this.quad(new PositionTextureVertex[] { v[1], v[0], v[3], v[2] }, faceUV, 0, texW, texH);
        this.quads[5] = this.quad(new PositionTextureVertex[] { v[4], v[5], v[6], v[7] }, faceUV, 4, texW, texH);
        if (mirror) {
            for (int i = 0; i < this.quads.length; i++) {
                this.quads[i].flipFace();
            }
        }
    }

    private TexturedQuad quad(PositionTextureVertex[] verts, int[] faceUV, int offset, float texW, float texH) {
        int u = faceUV[offset], v = faceUV[offset + 1], w = faceUV[offset + 2], h = faceUV[offset + 3];
        return new TexturedQuad(verts, u, v, u + w, v + h, texW, texH);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(Tessellator tessellator, float scale) {
        for (int i = 0; i < this.quads.length; i++) {
            this.quads[i].draw(tessellator, scale);
        }
    }
}
