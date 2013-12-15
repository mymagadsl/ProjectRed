package mrtjp.projectred.illumination;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mrtjp.projectred.core.Configurator;
import mrtjp.projectred.core.PRColors;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHalo
{
    public static RenderHalo instance = new RenderHalo();

    private static List<LightCache> renderQueue = new LinkedList<LightCache>();

    private static class LightCache
    {
        final BlockCoord pos;
        final int color;
        final Cuboid6 cube;
        final int multipartSlot;
        final Translation t;

        public LightCache(int x, int y, int z, int colorIndex, int slot, Cuboid6 cube)
        {
            this.pos = new BlockCoord(x, y, z);
            this.color = colorIndex;
            this.multipartSlot = slot;
            this.cube = cube;
            t = new Translation(x, y, z);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof LightCache)
            {
                LightCache o2 = (LightCache) o;
                return o2.pos.equals(pos) && o2.cube.min.equalsT(cube.min) && o2.cube.max.equalsT(cube.max);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return pos.hashCode();
        }
    }

    public static void addLight(int x, int y, int z, int color, int slot, Cuboid6 box)
    {
        renderQueue.add(new LightCache(x, y, z, color, slot, box));
    }

    @ForgeSubscribe
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        Tessellator tess = Tessellator.instance;
        WorldClient w = event.context.theWorld;

        GL11.glPushMatrix();
        RenderUtils.translateToWorldCoords(event.context.mc.renderViewEntity, event.partialTicks);
        prepareRenderState();

        Iterator<LightCache> it = renderQueue.iterator();
        while(it.hasNext())
        {
            LightCache cc = it.next();
            renderHalo(tess, w, cc);
        }

        restoreRenderState();
        GL11.glPopMatrix();
    }

    public static void prepareRenderState()
    {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        CCRenderState.reset();
        CCRenderState.startDrawing(7);
    }

    public static void restoreRenderState()
    {
        CCRenderState.draw();
        GL11.glDepthMask(true);
        GL11.glColor3f(1, 1, 1);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void renderHalo(Tessellator tess, World world, LightCache cc)
    {
        CCRenderState.setBrightness(world, cc.pos.x, cc.pos.y, cc.pos.z);
        renderHalo(tess, cc.cube, cc.color, cc.t);
    }

    public static void renderHalo(Tessellator tess, Cuboid6 cuboid, int colour, Transformation t)
    {
        tess.setColorRGBA_I(PRColors.VALID_COLORS[colour].rgb, 128);
        RenderUtils.renderBlock(cuboid, 0, t, null, null);
    }
}
