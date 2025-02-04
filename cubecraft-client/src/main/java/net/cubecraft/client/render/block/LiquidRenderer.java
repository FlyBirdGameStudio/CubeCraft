package net.cubecraft.client.render.block;

import me.gb2022.commons.ColorUtil;
import me.gb2022.commons.container.Vector3;
import me.gb2022.commons.registry.TypeItem;
import me.gb2022.quantum3d.render.vertex.VertexBuilder;
import me.gb2022.quantum3d.texture.Texture2DTileMap;
import net.cubecraft.client.render.Textures;
import net.cubecraft.client.render.chunk.container.ChunkLayerContainerFactory;
import net.cubecraft.client.render.chunk.container.ChunkLayerContainers;
import net.cubecraft.client.render.model.object.Vertex;
import net.cubecraft.client.resource.TextureAsset;
import net.cubecraft.resource.MultiAssetContainer;
import net.cubecraft.util.register.Registered;
import net.cubecraft.world.BlockAccessor;
import net.cubecraft.world.block.EnumFacing;
import net.cubecraft.world.block.access.BlockAccess;
import net.cubecraft.world.block.property.BlockPropertyDispatcher;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.Objects;

@TypeItem("cubecraft:liquid")
public final class LiquidRenderer implements IBlockRenderer {
    private final TextureAsset calmTexture;
    private final TextureAsset flowTexture;

    public LiquidRenderer(TextureAsset calmTexture, TextureAsset flowTexture) {
        this.calmTexture = calmTexture;
        this.flowTexture = flowTexture;
    }

    public boolean shouldRender(int current, BlockAccess blockAccess, BlockAccessor world, long x, long y, long z) {
        Vector3<Long> pos = EnumFacing.findNear(x, y, z, 1, current);
        BlockAccess near = world.getBlockAccess(pos.x(), pos.y(), pos.z());
        boolean nearSolid = BlockPropertyDispatcher.isSolid(near);
        boolean nearEquals = Objects.equals(near.getBlockId(), blockAccess.getBlockId());
        if (current == 0 && !nearEquals) {
            return true;
        }
        return !(nearSolid || nearEquals);
    }

    @Override
    public void render(BlockAccess block, BlockAccessor accessor, Registered<ChunkLayerContainerFactory.Provider> layer, int face, float x, float y, float z, VertexBuilder builder) {
        var h00 = 0.875f;
        var h01 = 0.875f;
        var h10 = 0.875f;
        var h11 = 0.875f;

        if (block != null) {
            if (block.getNear(EnumFacing.Up).getBlockId() == block.getBlockId()) {
                h00 = h01 = h10 = h11 = 1f;
            }

            if (layer != ChunkLayerContainers.TRANSPARENT) {
                return;
            }
        }

        if(block == null || accessor == null){
            renderFace(block, face, builder, accessor, h00, h01, h10, h11, x, y, z);
            return;
        }

        if (shouldRender(face, block, accessor, block.getX(), block.getY(), block.getZ())) {
            renderFace(block, face, builder, accessor, h00, h01, h10, h11, x, y, z);
        }
    }

    @Override
    public void provideTileMapItems(MultiAssetContainer<TextureAsset> list) {
        list.addResource("cubecraft:transparent_block", this.calmTexture);
        list.addResource("cubecraft:transparent_block", this.flowTexture);
    }

    //todo
    public void renderFace(BlockAccess block, int face, VertexBuilder builder, BlockAccessor w, float h00, float h01, float h10, float h11, double renderX, double renderY, double renderZ) {
        Texture2DTileMap terrain = Textures.TERRAIN_TRANSPARENT.get();

        String path = face == 0 || face == 1 ? this.calmTexture.getAbsolutePath() : this.flowTexture.getAbsolutePath();


        var x = block.getX();
        var y = block.getY();
        var z = block.getZ();

        float u0 = terrain.exactTextureU(path, 0);
        float u1 = terrain.exactTextureU(path, 0.5f);
        float v0 = terrain.exactTextureV(path, 0);
        float v1 = terrain.exactTextureV(path, 1 / 32f);

        if (face == 0 || face == 1) {
            u0 = terrain.exactTextureU(path, 0);
            u1 = terrain.exactTextureU(path, 1);
            v0 = terrain.exactTextureV(path, 0);
            v1 = terrain.exactTextureV(path, 1 / 32f);
        }

        Vector3f v000 = new Vector3f(0, 0, 0);
        Vector3f v001 = new Vector3f(0, 0, 1);
        Vector3f v010 = new Vector3f(0, h00, 0);
        Vector3f v011 = new Vector3f(0, h01, 1);
        Vector3f v100 = new Vector3f(1, 0, 0);
        Vector3f v101 = new Vector3f(1, 0, 1);
        Vector3f v110 = new Vector3f(1, h10, 0);
        Vector3f v111 = new Vector3f(1, h11, 1);

        Vector3f render = new Vector3f((float) renderX, (float) renderY, (float) renderZ);
        int c = 0x3F76E4;
        if (face == 0) {
            Vector3f faceColor = new Vector3f(ColorUtil.int1ToFloat3(c));

            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v011).add(render), new Vector2d(u0, v1), faceColor), v001, w, x, y, z, 0)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v010).add(render), new Vector2d(u0, v0), faceColor), v000, w, x, y, z, 0)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v110).add(render), new Vector2d(u1, v0), faceColor), v100, w, x, y, z, 0)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v111).add(render), new Vector2d(u1, v1), faceColor), v101, w, x, y, z, 0)
                    .draw(builder);

            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v111).add(render), new Vector2d(u1, v1), faceColor), v111, w, x, y, z, 0)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v110).add(render), new Vector2d(u1, v0), faceColor), v110, w, x, y, z, 0)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v010).add(render), new Vector2d(u0, v0), faceColor), v010, w, x, y, z, 0)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v011).add(render), new Vector2d(u0, v1), faceColor), v011, w, x, y, z, 0)
                    .draw(builder);
            return;
        }
        if (face == 1) {
            Vector3f faceColor = new Vector3f(ColorUtil.int1ToFloat3(c));
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v001).add(render), new Vector2d(u0, v1), faceColor), v001, w, x, y, z, 1)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v000).add(render), new Vector2d(u0, v0), faceColor), v000, w, x, y, z, 1)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v100).add(render), new Vector2d(u1, v0), faceColor), v100, w, x, y, z, 1)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v101).add(render), new Vector2d(u1, v1), faceColor), v101, w, x, y, z, 1)
                    .draw(builder);

            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v101).add(render), new Vector2d(u1, v1), faceColor), v111, w, x, y, z, 1)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v100).add(render), new Vector2d(u1, v0), faceColor), v110, w, x, y, z, 1)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v000).add(render), new Vector2d(u0, v0), faceColor), v010, w, x, y, z, 1)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v001).add(render), new Vector2d(u0, v1), faceColor), v011, w, x, y, z, 1)
                    .draw(builder);
            return;
        }
        if (face == 2) {
            Vector3f faceColor = new Vector3f(ColorUtil.int1ToFloat3(c));
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v011).add(render), new Vector2d(u1, v0), faceColor), v010, w, x, y, z, 3)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v111).add(render), new Vector2d(u0, v0), faceColor), v110, w, x, y, z, 3)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v101).add(render), new Vector2d(u0, v1), faceColor), v100, w, x, y, z, 3)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v001).add(render), new Vector2d(u1, v1), faceColor), v000, w, x, y, z, 3)
                    .draw(builder);

            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v011).add(render), new Vector2d(u0, v0), faceColor), v011, w, x, y, z, 2)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v001).add(render), new Vector2d(u0, v1), faceColor), v001, w, x, y, z, 2)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v101).add(render), new Vector2d(u1, v1), faceColor), v101, w, x, y, z, 2)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v111).add(render), new Vector2d(u1, v0), faceColor), v111, w, x, y, z, 2)
                    .draw(builder);
            return;
        }
        if (face == 3) {
            Vector3f faceColor = new Vector3f(ColorUtil.int1ToFloat3(c));
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v010).add(render), new Vector2d(u0, v0), faceColor), v011, w, x, y, z, 2)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v000).add(render), new Vector2d(u0, v1), faceColor), v001, w, x, y, z, 2)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v100).add(render), new Vector2d(u1, v1), faceColor), v101, w, x, y, z, 2)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v110).add(render), new Vector2d(u1, v0), faceColor), v111, w, x, y, z, 2)
                    .draw(builder);

            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v010).add(render), new Vector2d(u1, v0), faceColor), v010, w, x, y, z, 3)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v110).add(render), new Vector2d(u0, v0), faceColor), v110, w, x, y, z, 3)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v100).add(render), new Vector2d(u0, v1), faceColor), v100, w, x, y, z, 3)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v000).add(render), new Vector2d(u1, v1), faceColor), v000, w, x, y, z, 3)
                    .draw(builder);
            return;
        }
        if (face == 4) {
            Vector3f faceColor = new Vector3f(ColorUtil.int1ToFloat3(c));
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v111).add(render), new Vector2d(u1, v0), faceColor), v011, w, x, y, z, 5)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v110).add(render), new Vector2d(u0, v0), faceColor), v010, w, x, y, z, 5)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v100).add(render), new Vector2d(u0, v1), faceColor), v000, w, x, y, z, 5)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v101).add(render), new Vector2d(u1, v1), faceColor), v001, w, x, y, z, 5)
                    .draw(builder);


            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v101).add(render), new Vector2d(u0, v1), faceColor), v100, w, x, y, z, 4)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v100).add(render), new Vector2d(u1, v1), faceColor), v101, w, x, y, z, 4)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v110).add(render), new Vector2d(u1, v0), faceColor), v111, w, x, y, z, 4)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v111).add(render), new Vector2d(u0, v0), faceColor), v110, w, x, y, z, 4)
                    .draw(builder);
            return;
        }
        if (face == 5) {
            Vector3f faceColor = new Vector3f(ColorUtil.int1ToFloat3(c));
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v011).add(render), new Vector2d(u1, v0), faceColor), v011, w, x, y, z, 5)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v010).add(render), new Vector2d(u0, v0), faceColor), v010, w, x, y, z, 5)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v000).add(render), new Vector2d(u0, v1), faceColor), v000, w, x, y, z, 5)
                    .draw(builder);
            BlockBakery.bakeVertex(Vertex.create(new Vector3f(v001).add(render), new Vector2d(u1, v1), faceColor), v001, w, x, y, z, 5)
                    .draw(builder);
        }
    }
}
