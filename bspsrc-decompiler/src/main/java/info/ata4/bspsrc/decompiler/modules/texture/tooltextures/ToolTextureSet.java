package info.ata4.bspsrc.decompiler.modules.texture.tooltextures;

import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions.CsgoToolTextureDefinitions;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions.CssToolTextureDefinition;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions.SourceToolTextureDefinition;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the set of tooltextures present in each game
 */
public enum ToolTextureSet {

    SOURCE_2013(SourceAppId.UNKNOWN, SourceToolTextureDefinition.getAll()),
    COUNTER_STRIKE_SOURCE(
            SourceAppId.COUNTER_STRIKE_SOURCE,
            SOURCE_2013.builder()
                    .putToolTextureDefinitions(CssToolTextureDefinition.getAll())
                    .build()
    ),
    COUNTER_STRIKE_GO(
            SourceAppId.COUNTER_STRIKE_GO,
            SOURCE_2013.builder()
                    .putToolTextureDefinitions(CsgoToolTextureDefinitions.getAll())
                    .build()
    );

    private final int appId;
    private final Map<String, ToolTextureDefinition> toolTextureDefinitions;

    ToolTextureSet(int appId, Map<String, ToolTextureDefinition> toolTextureDefinitions) {
        this.appId = appId;
        this.toolTextureDefinitions = Map.copyOf(toolTextureDefinitions);
    }

    private Builder builder() {
        return new Builder(toolTextureDefinitions);
    }

    public static Map<String, ToolTextureDefinition> forGame(int appId) {
        return Arrays.stream(values())
                .filter(toolTextureSets -> toolTextureSets.appId == appId)
                .findAny()
                .orElse(SOURCE_2013)
                .toolTextureDefinitions;
    }

    /**
     * A Builder class for building a set of tooltexture definition
     */
    private static class Builder {

        private final Map<String, ToolTextureDefinition> definitions = new HashMap<>();

        public Builder(Map<String, ToolTextureDefinition> definitions) {
            this.definitions.putAll(definitions);
        }

        public Builder putToolTextureDefinitions(Map<String, ToolTextureDefinition> definitions) {
            this.definitions.putAll(definitions);
            return this;
        }

        public Map<String, ToolTextureDefinition> build() {
            return new HashMap<>(definitions);
        }
    }
}
