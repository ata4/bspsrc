package info.ata4.bspsrc.decompiler.modules.texture;

import info.ata4.bspsrc.common.util.PathUtil;
import info.ata4.bspsrc.decompiler.VmfWriter;
import info.ata4.bspsrc.decompiler.modules.geom.BrushSideFaceMapper;
import info.ata4.bspsrc.decompiler.util.AABB;
import info.ata4.bspsrc.decompiler.util.ConvexVolume;
import info.ata4.bspsrc.lib.app.SourceAppId;
import info.ata4.bspsrc.lib.struct.BrushFlag;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.bspsrc.lib.struct.DBrush;
import info.ata4.bspsrc.lib.struct.SurfaceFlag;
import info.ata4.bspsrc.lib.vector.Vector3d;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ToolTextureMatcherTestHelper {
    public static void main(String[] args) throws IOException, InterruptedException {
        var dir = Path.of("bspsrc-decompiler\\src\\test\\resources\\info\\ata4\\bspsrc\\decompiler\\modules\\texture");

        var gameDirCsgo = Path.of("D:\\SteamLibrary\\steamapps\\common\\Counter-Strike Global Offensive\\csgo");
        var gameDirL4d2 = Path.of("D:\\SteamLibrary\\steamapps\\common\\Left 4 Dead 2\\left4dead2");
        var gameDirGmod = Path.of("D:\\SteamLibrary\\steamapps\\common\\GarrysMod\\garrysmod");

        writeGmodBsp(dir, gameDirGmod, gameDirGmod.getParent().resolve("bin", "vbsp.exe"));
        writeCsgoBsp(dir, gameDirCsgo, gameDirCsgo.getParent().resolve("bin", "vbsp.exe"));
        writeL4d2Bsp(dir, gameDirL4d2, gameDirL4d2.getParent().resolve("bin", "vbsp.exe"));
    }

    private static void writeGmodBsp(
            Path dir,
            Path gameDir,
            Path vbspFile
    ) throws IOException, InterruptedException {
        var vmfFile = dir.resolve("gmod_tooltextures.vmf");
        var textures = List.of(
                List.of(
                        ToolTexture.BLOCKBULLETS,
                        ToolTexture.INVIS,
                        ToolTexture.INVISLADDER,
                        ToolTexture.NODRAW,
                        ToolTexture.BLOCKLOS,
                        ToolTexture.BLOCKLIGHT,
                        ToolTexture.TRIGGER
                ),
                List.of(
                        ToolTexture.HINT,
                        ToolTexture.SKIP
                ),
                List.of(
                        ToolTexture.CLIP,
                        ToolTexture.NPCCLIP,
                        ToolTexture.PLAYERCLIP
                ),
                List.of(
                        ToolTexture.CLIP_CONCRETE,
                        ToolTexture.CLIP_DIRT,
                        ToolTexture.CLIP_GLASS,
                        ToolTexture.CLIP_GRASS,
                        ToolTexture.CLIP_GRAVEL,
                        ToolTexture.CLIP_METAL,
//                        ToolTexture.CLIP_METAL_SAND_BARREL, // is replaced by tools/toolsclip_glass by vbsp??
                        ToolTexture.CLIP_METALGRATE,
                        ToolTexture.CLIP_METALVEHICEL
                ),
                List.of(
                        ToolTexture.CLIP_PLASTIC,
                        ToolTexture.CLIP_RUBBER,
                        ToolTexture.CLIP_RUBBERTIRE,
                        ToolTexture.CLIP_SAND,
//                        ToolTexture.CLIP_SNOW, // gmod doesn't have this one
                        ToolTexture.CLIP_TILE,
                        ToolTexture.CLIP_WOOD,
//                        ToolTexture.CLIP_WOOD_BASKET, // is replaced by tools/toolsclip_glass by vbsp??
                        ToolTexture.CLIP_WOOD_CRATE
                ),
                List.of(
                        ToolTexture.SKYBOX,
                        ToolTexture.SKYBOX2D,
                        "tools/toolsskyfog"
                )
        );

        writeVmf(vmfFile, textures, SourceAppId.GARRYS_MOD);
        compile(dir, vmfFile, vbspFile, gameDir, List.of());
    }
    
    private static void writeCsgoBsp(
            Path dir,
            Path gameDir,
            Path vbspFile
    ) throws IOException, InterruptedException {
        var vmfFile = dir.resolve("csgo_tooltextures.vmf");
        var textures = List.of(
                List.of(
                        ToolTexture.BLOCKBULLETS,
                        ToolTexture.INVIS,
                        ToolTexture.INVISLADDER,
                        ToolTexture.NODRAW,
                        ToolTexture.BLOCKLOS,
                        ToolTexture.BLOCKLIGHT,
                        ToolTexture.TRIGGER
                ),
                List.of(
                        ToolTexture.HINT,
                        ToolTexture.SKIP
                ),
                List.of(
                        ToolTexture.CLIP,
                        ToolTexture.NPCCLIP,
                        ToolTexture.PLAYERCLIP
                ),
                List.of(
                        ToolTexture.CLIP_CONCRETE,
                        ToolTexture.CLIP_DIRT,
                        ToolTexture.CLIP_GLASS,
                        ToolTexture.CLIP_GRASS,
                        ToolTexture.CLIP_GRAVEL,
                        ToolTexture.CLIP_METAL,
                        ToolTexture.CLIP_METAL_SAND_BARREL,
                        ToolTexture.CLIP_METALGRATE,
                        ToolTexture.CLIP_METALVEHICEL
                ),
                List.of(
                        ToolTexture.CLIP_PLASTIC,
                        ToolTexture.CLIP_RUBBER,
                        ToolTexture.CLIP_RUBBERTIRE,
                        ToolTexture.CLIP_SAND,
                        ToolTexture.CLIP_SNOW,
                        ToolTexture.CLIP_TILE,
                        ToolTexture.CLIP_WOOD,
                        ToolTexture.CLIP_WOOD_BASKET,
                        ToolTexture.CLIP_WOOD_CRATE
                ),
                List.of(
                        ToolTexture.SKYBOX,
                        ToolTexture.SKYBOX2D,
                        "tools/toolsskyfog"
                ),
                List.of(
                        ToolTexture.CSGO_GRENADECLIP,
                        ToolTexture.CSGO_DRONECLIP
//                        "tools/toolsblockbomb" // identical to tools/toolsblockbullets
                )
        );

        writeVmf(vmfFile, textures, SourceAppId.COUNTER_STRIKE_GO);
        compile(dir, vmfFile, vbspFile, gameDir, List.of());
    }
    
    private static void writeL4d2Bsp(
            Path dir,
            Path gameDir,
            Path vbspFile
    ) throws IOException, InterruptedException {
        var vmfFile = dir.resolve("l4d2_tooltextures.vmf");
        var textures = List.of(
                List.of(
                        ToolTexture.BLOCKBULLETS,
                        ToolTexture.INVIS,
                        ToolTexture.INVISLADDER,
                        ToolTexture.NODRAW,
                        ToolTexture.NODRAW_METAL,
                        ToolTexture.BLOCKLOS,
                        ToolTexture.BLOCKLIGHT,
                        ToolTexture.TRIGGER
                ),
                List.of(
                        ToolTexture.HINT,
                        ToolTexture.SKIP
                ),
                List.of(
                        ToolTexture.CLIP,
                        ToolTexture.NPCCLIP,
                        ToolTexture.PLAYERCLIP
                ),
                List.of(
                        ToolTexture.SKYBOX,
                        ToolTexture.SKYBOX2D,
                        "tools/toolsskyfog"
                ),
                List.of(
                        ToolTexture.CLIMB,
                        "tools/climb_alpha",
                        "tools/climb_versus",
                        "tools/hulkwall",
                        "tools/hulkwallglow",
                        "effects/tankwall",
                        ToolTexture.INVISMETAL
                )
        );

        writeVmf(vmfFile, textures, SourceAppId.LEFT_4_DEAD_2);
        compile(dir, vmfFile, vbspFile, gameDir, List.of());
    }

    private static void writeVmf(
            Path vmfFile,
            List<List<String>> textureNames,
            int appId
    ) throws IOException {
        var bounds = new AABB(
                new Vector3d(0, 0, 0),
                new Vector3d(512, 512, 512)
        );

        var uid = new AtomicInteger();
        try (var writer = new VmfWriter(new PrintWriter(vmfFile.toFile()), 8, 4, 4))
        {
            writer.start("world");
            writer.put("id", uid.getAndIncrement());
            writer.put("classname", "worldspawn");
            writer.put("appId", appId);
            writer.put("textureNames", textureNames.stream()
                    .map(strings -> String.join(" ", strings))
                    .collect(Collectors.joining(";")));
            
            writeRoom(writer, uid, bounds);
            writeCubes(writer, uid, bounds, textureNames);

            writer.end("world");

            writer.start("entity");
            writer.put("id", uid.getAndIncrement());
            writer.put("classname", "info_player_start");
            writer.put("origin", bounds.getMin().add(bounds.getMax()).scalar(0.5).withZ(bounds.getMin().z() + 8));
            writer.end("entity");
        }
    }

    private static void writeCubes(
            VmfWriter writer,
            AtomicInteger uidCounter,
            AABB bounds,
            List<List<String>> textures
    )
    {
        for (int y = 0; y < textures.size(); y++)
        {
            List<String> textureGroup = textures.get(y);
            for (int x = 0; x < textureGroup.size(); x++)
            {
                var texture = textureGroup.get(x);
                writeBrush(writer, uidCounter, ConvexVolume.aabb(boundsForPosition(x, y, 0, bounds.getMin())), texture);
            }
        }
    }

    private static AABB boundsForPosition(
            int x,
            int y,
            int z,
            Vector3d origin
    )
    {
        var size = 16.;
        var center = centerForPosition(x, y, z, origin);
        return new AABB(
                center.sub(size / 2),
                center.add(size / 2)
        );
    }

    static Vector3d centerForPosition(int x, int y, int z, Vector3d origin)
    {
        var margin = 16.;
        var spacing = 32.;
        var offset = new Vector3d(x, y, z).scalar(spacing).add(margin);
        return origin.add(offset);
    }

    private static void writeRoom(
            VmfWriter writer,
            AtomicInteger uidCounter,
            AABB aabb
    )
    {
        var wallThickness = 32;
        var min = aabb.getMin();
        var max = aabb.getMax();

        var floorVolume = ConvexVolume.aabb(
                new Vector3d(min.x(), min.y(), min.z() - wallThickness),
                new Vector3d(max.x(), max.y(), min.z())
        );
        var roofVolume = ConvexVolume.aabb(
                new Vector3d(min.x(), min.y(), max.z()),
                new Vector3d(max.x(), max.y(), max.z() + wallThickness)
        );
        writeBrush(writer, uidCounter, floorVolume, "DEV/DEV_MEASUREGENERIC01B");
        writeBrush(writer, uidCounter, roofVolume, "DEV/DEV_MEASUREGENERIC01B");

        var xNegWall = ConvexVolume.aabb(
                new Vector3d(min.x() - wallThickness, min.y(), min.z()),
                new Vector3d(min.x(), max.y(), max.z())
        );
        var yNegWall = ConvexVolume.aabb(
                new Vector3d(min.x(), min.y() - wallThickness, min.z()),
                new Vector3d(max.x(), min.y(), max.z())
        );
        var xPosWall = ConvexVolume.aabb(
                new Vector3d(max.x(), min.y(), min.z()),
                new Vector3d(max.x() + wallThickness, max.y(), max.z())
        );
        var yPosWall = ConvexVolume.aabb(
                new Vector3d(min.x(), max.y(), min.z()),
                new Vector3d(max.x(), max.y() + wallThickness, max.z())
        );
        writeBrush(writer, uidCounter, xNegWall, "DEV/DEV_MEASUREWALL01A");
        writeBrush(writer, uidCounter, yNegWall, "DEV/DEV_MEASUREWALL01A");
        writeBrush(writer, uidCounter, xPosWall, "DEV/DEV_MEASUREWALL01A");
        writeBrush(writer, uidCounter, yPosWall, "DEV/DEV_MEASUREWALL01A");
    }

    private static void writeBrush(
            VmfWriter writer,
            AtomicInteger uidCounter,
            ConvexVolume<?> brushVolume,
            String textureName
    )
    {
        writer.start("solid");
        writer.put("id", uidCounter.getAndIncrement());

        for (var face : brushVolume.faces()) {
            var plane = face.winding().buildPlane();

            var texture = new Texture();
            texture.texture = textureName;

            var ev12 = plane[1].sub(plane[0]);
            var ev13 = plane[2].sub(plane[0]);
            var normal = ev12.cross(ev13).normalize();

            // calculate the projections of the surface normal onto the world axes
            var dotX = Math.abs(Vector3d.BASE_VECTOR_X.dot(normal));
            var dotY = Math.abs(Vector3d.BASE_VECTOR_Y.dot(normal));
            var dotZ = Math.abs(Vector3d.BASE_VECTOR_Z.dot(normal));

            Vector3d vdir;

            // if the projection of the surface normal onto the z-axis is greatest
            if (dotZ > dotX && dotZ > dotY) {
                // use y-axis as basis
                vdir = Vector3d.BASE_VECTOR_Y;
            } else {
                // otherwise use z-axis as basis
                vdir = Vector3d.BASE_VECTOR_Z;
            }

            var tv1 = normal.cross(vdir).normalize(); // 1st tex vector
            var tv2 = normal.cross(tv1).normalize();  // 2nd tex vector

            texture.u = new TextureAxis(tv1);
            texture.v = new TextureAxis(tv2);

            writer.start("side");
            writer.put("id", uidCounter.getAndIncrement());
            writer.put("plane", plane[0], plane[1], plane[2]);
            writer.put(texture);
            writer.end("side");
        }

        writer.end("solid");
    }


    public static Path compile(
            Path outputDir,
            Path vmfFile,
            Path vbspFile,
            Path gameDir,
            List<String> vbspArgs
    ) throws IOException, InterruptedException {
        var command = new ArrayList<String>();
        command.add(vbspFile.toString());
        command.add("-game");
        command.add(gameDir.toString());
        command.addAll(vbspArgs);
        command.add(vmfFile.toString());

        var stdout = outputDir.resolve("recompilation_stdout.log");
        var stderr = outputDir.resolve("recompilation_stderr.log");

        var exitCode = new ProcessBuilder(command) // "-blocksize", "2048",
                .redirectOutput(stdout.toFile())
                .redirectError(stderr.toFile())
                .start()
                .waitFor();

        if (exitCode != 0) {
            throw new IOException("vBsp finished with %d".formatted(exitCode));
        }

        return vmfFile.resolveSibling(PathUtil.nameWithoutExtension(vmfFile).orElseThrow() + ".bsp");
    }
    
    record CubeData(
            Set<BrushFlag> brushFlags,
            Set<SurfaceFlag> surfaceFlags,
            String textureName,
            String fixedTextureName,
            boolean needsFixing
    ) {}
    static CubeData queryCube(
            BspData bspData,
            Vector3d center,
            ToolTextureMatcher toolTextureMatcher,
            BrushSideFaceMapper brushSideMapping
    )
    {
        var brush = bspData.brushes.stream()
                .filter(dBrush -> isPositionInBrush(bspData, dBrush, center))
                .findAny()
                .orElse(null);

        if (brush == null) {
            return null;
        }

        var datas = IntStream.range(brush.fstside, brush.fstside + brush.numside)
                .mapToObj(brushSideI -> {
                    var brushSide = bspData.brushSides.get(brushSideI);
                    var texinfo = bspData.texinfos.get(brushSide.texinfo);
                    var texdata = bspData.texdatas.get(texinfo.texdata);
                    var texname = bspData.texnames.get(texdata.texname);

                    return new CubeData(
                            brush.contents,
                            texinfo.flags,
                            texname,
                            toolTextureMatcher.fixToolTexture(texname, brush.contents, texinfo.flags).orElse(null),
                            brushSideMapping.getOrigFaceIndex(brushSideI).isEmpty()
                    );
                })
                .distinct()
                .toList();
        
        if (datas.size() != 1)
            throw new RuntimeException("Cube has differing brush sides: %s".formatted(datas));

        return datas.getFirst();
    }
    private static boolean isPositionInBrush(
            BspData bspData,
            DBrush brush,
            Vector3d position
    )
    {
        var brushSides = bspData.brushSides.subList(brush.fstside, brush.fstside + brush.numside);
        return brushSides.stream()
                .map(brushSide -> {
                    var plane = bspData.planes.get(brushSide.pnum);
                    return position.dot(plane.normal.toDouble()) - plane.dist;
                })
                .allMatch(dist -> dist < 0);
    }
}
