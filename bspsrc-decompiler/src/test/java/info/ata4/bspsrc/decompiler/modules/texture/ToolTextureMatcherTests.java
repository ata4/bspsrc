package info.ata4.bspsrc.decompiler.modules.texture;

import info.ata4.bspsrc.decompiler.modules.geom.BrushSideFaceMapper;
import info.ata4.bspsrc.decompiler.util.WindingFactory;
import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.exceptions.BspException;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.bspsrc.lib.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static info.ata4.bspsrc.decompiler.modules.texture.ToolTextureMatcherTestHelper.centerForPosition;
import static info.ata4.bspsrc.decompiler.modules.texture.ToolTextureMatcherTestHelper.queryCube;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("Test ToolTextureMatcher")
public class ToolTextureMatcherTests {

    private static final Logger L = LogManager.getLogger();
    
    @TempDir
    Path tempDir;
    
    private static final List<String> bspsResources = List.of(
            "csgo_tooltextures.bsp",
            "l4d2_tooltextures.bsp"
    );

    @DisplayName("Test tooltexture fixing with bsps")
    @TestFactory
    Stream<DynamicNode> testMatching() {
        return bspsResources.stream()
                .map(resource -> {
                    BspFile bsp;
                    try {
                        bsp = loadBspFromResource(resource);
                    } catch (IOException | BspException e) {
                        throw new RuntimeException("Error loading BSP", e);
                    }

                    var reader = new BspFileReader(bsp);
                    reader.loadEntities();
                    reader.loadBrushes();
                    reader.loadBrushSides();
                    reader.loadPlanes();
                    reader.loadTexInfo();
                    reader.loadTexData();

                    var worldspawnEntity = reader.getData().entities.stream()
                            .filter(entity -> entity.getClassName().equals("worldspawn"))
                            .findAny()
                            .orElseThrow();

                    var appId = Integer.parseInt(worldspawnEntity.getValue("appId"));
                    var textureNames = Arrays.stream(worldspawnEntity.getValue("textureNames").split(";"))
                            .map(s -> List.of(s.split(" ")))
                            .toList();

                    var toolTextureMatcher = ToolTextureMatcher.forAppId(appId);
                    var windingFactory = WindingFactory.forAppId(appId);
                    var brushSideFaceMapper = new BrushSideFaceMapper(reader, windingFactory);
                    brushSideFaceMapper.load();

                    return dynamicContainer(
                            resource,
                            generateSubTests(reader.getData(), textureNames, toolTextureMatcher, brushSideFaceMapper)
                    );
                });
    }
    
    private BspFile loadBspFromResource(String resource) throws IOException, BspException {
        var bspFile = tempDir.resolve(resource);
        try (var is = ToolTextureMatcherTests.class.getResourceAsStream(resource)) {
            Files.copy(is, bspFile, StandardCopyOption.REPLACE_EXISTING);
        }

        var bsp = new BspFile();
        bsp.load(bspFile);
        return bsp;
    }

    private static ArrayList<DynamicNode> generateSubTests(
            BspData bspData,
            List<List<String>> textureNames,
            ToolTextureMatcher toolTextureMatcher,
            BrushSideFaceMapper brushSideMapping
    )
    {
        var subTests = new ArrayList<DynamicNode>();
        for (int y = 0; y < textureNames.size(); y++)
        {
            List<String> textureGroup = textureNames.get(y);
            for (int x = 0; x < textureGroup.size(); x++)
            {
                var texture = textureGroup.get(x);
                var center = centerForPosition(x, y, 0, Vector3d.NULL);
                
                var test = dynamicTest(texture, () -> {
                    var cubeData = queryCube(bspData, center, toolTextureMatcher, brushSideMapping);
                    if (cubeData == null)
                        throw new RuntimeException("No cube exits for texture '%s'".formatted(texture));
                    
                    L.info("brush flags: {}", cubeData.brushFlags().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")));
                    L.info("surface flags: {}", cubeData.surfaceFlags().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")));
                    L.info("texture expected: {}", texture);
                    L.info("texture actual: {}", cubeData.textureName());
                    L.info("texture fixed: {}", cubeData.fixedTextureName());
                    L.info("needs fixing: {}", cubeData.needsFixing());

                    assumeTrue(cubeData.needsFixing(), "Texture '%s' doesn't need fixing.".formatted(texture));
                    assertEquals(
                            texture.toLowerCase(Locale.ROOT),
                            cubeData.fixedTextureName() == null ? null : cubeData.fixedTextureName().toLowerCase(Locale.ROOT),
                            "Fixed texture is not original."
                    );
                });
                subTests.add(test);
            }
        }
        return subTests;
    }
}
