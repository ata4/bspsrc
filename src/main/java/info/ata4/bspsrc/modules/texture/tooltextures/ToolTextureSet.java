package info.ata4.bspsrc.modules.texture.tooltextures;

import info.ata4.bsplib.app.SourceAppID;
import info.ata4.bspsrc.modules.texture.tooltextures.definitions.CssToolTextureDefinition;
import info.ata4.bspsrc.modules.texture.tooltextures.definitions.SourceToolTextureDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the set of tooltextures present in each game
 */
public enum ToolTextureSet {

    SOURCE_2013(SourceAppID.UNKNOWN, SourceToolTextureDefinition.getAll()),
    COUNTER_STRIKE_SOURCE(
            SourceAppID.COUNTER_STRIKE_SOURCE,
            SOURCE_2013.builder()
                    .putToolTextureDefinitions(CssToolTextureDefinition.getAll())
                    .build()
    );

    private final int appId;
    private final Map<String, ToolTextureDefinition> toolTextureDefinitions;

    ToolTextureSet(int appId, Map<String, ToolTextureDefinition> toolTextureDefinitions) {
        this.appId = appId;
        this.toolTextureDefinitions = Collections.unmodifiableMap(new HashMap<>(toolTextureDefinitions));
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
