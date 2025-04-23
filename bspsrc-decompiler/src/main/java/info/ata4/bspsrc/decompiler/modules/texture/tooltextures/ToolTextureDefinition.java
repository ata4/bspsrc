package info.ata4.bspsrc.decompiler.modules.texture.tooltextures;

import info.ata4.bspsrc.lib.struct.BrushFlag;
import info.ata4.bspsrc.lib.struct.SurfaceFlag;

import java.util.*;

public interface ToolTextureDefinition {

    /**
     * Note: While the engine may use some form of default surface property for materials which
     * have none specified, the vbsp optimizing process does seem to differentiate between these two.
     * <p>This is the reason we return an Optional here.
     *
     * @return an optional containing the surface property the texture has or an empty optional
     */
    Optional<String> getSurfaceProperty();

    /**
     * @return a map of brush flags that are either required(true) or forbidden(false) to have
     */
    Map<BrushFlag, Boolean> getBrushFlagsRequirements();

    /**
     * @return a map of surface flags that are either required(true) or forbidden(false) to have
     */
    Map<SurfaceFlag, Boolean> getSurfaceFlagsRequirements();

    /**
     * A Builder class for {@link ToolTextureDefinition}s
     */
    public class Builder {

        private String surfaceProperty;
        private final Map<BrushFlag, Boolean> brushFlagsRequirements = new HashMap<>();
        private final Map<SurfaceFlag, Boolean> surfaceFlagsRequirements = new HashMap<>();

        public Builder() {
            this((String) null);
        }

        public Builder(String surfaceProperty) {
            this.surfaceProperty = surfaceProperty;
        }

        public Builder(ToolTextureDefinition definition) {
            this(definition.getSurfaceProperty().orElse(null));

            brushFlagsRequirements.putAll(definition.getBrushFlagsRequirements());
            surfaceFlagsRequirements.putAll(definition.getSurfaceFlagsRequirements());
        }


        public Builder setSurfaceProperty(String surfaceProperty) {
            this.surfaceProperty = Objects.requireNonNull(surfaceProperty);
            return this;
        }

        public Builder setRequiredFlags(BrushFlag... brushFlags) {
            return setFlags(brushFlagsRequirements, true, brushFlags);
        }
        public Builder setForbiddenFlags(BrushFlag... brushFlags) {
            return setFlags(brushFlagsRequirements, false, brushFlags);
        }

        public Builder setRequiredFlags(SurfaceFlag... surfaceFlags) {
            return setFlags(surfaceFlagsRequirements, true, surfaceFlags);
        }
        public Builder setForbiddenFlags(SurfaceFlag... surfaceFlags) {
            return setFlags(surfaceFlagsRequirements, false, surfaceFlags);
        }

        public Builder setIrrelevantFlags(BrushFlag... brushFlags) {
            Arrays.asList(brushFlags).forEach(brushFlagsRequirements.keySet()::remove);
            return this;
        }
        public Builder setIrrelevantFlags(SurfaceFlag... surfaceFlags) {
            Arrays.asList(surfaceFlags).forEach(surfaceFlagsRequirements.keySet()::remove);
            return this;
        }

        @SafeVarargs
        private final <K> Builder setFlags(Map<K, Boolean> set, boolean state, K... ks) {
            for (K k : ks) {
                set.put(k, state);
            }
            return this;
        }

        public Builder clearBrushFlagRequirements() {
            brushFlagsRequirements.clear();
            return this;
        }
        public Builder clearSurfaceFlagRequirements() {
            surfaceFlagsRequirements.clear();
            return this;
        }

        public ToolTextureDefinition build() {
            return new ToolTextureDefinition() {

                private final String surfaceProperty = Builder.this.surfaceProperty;
                private final Map<SurfaceFlag, Boolean> surfaceFlagsRequirements =
                        Map.copyOf(Builder.this.surfaceFlagsRequirements);
                private final Map<BrushFlag, Boolean> brushFlagsRequirements =
                        Map.copyOf(Builder.this.brushFlagsRequirements);

                @Override
                public Optional<String> getSurfaceProperty() {
                    return Optional.ofNullable(surfaceProperty);
                }

                @Override
                public Map<BrushFlag, Boolean> getBrushFlagsRequirements() {
                    return brushFlagsRequirements;
                }

                @Override
                public Map<SurfaceFlag, Boolean> getSurfaceFlagsRequirements() {
                    return surfaceFlagsRequirements;
                }
            };
        }
    }
}
