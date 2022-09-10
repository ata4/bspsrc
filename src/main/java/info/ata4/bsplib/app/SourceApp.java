/*
 ** 2012 Februar 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.app;

import info.ata4.log.LogUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static info.ata4.util.JavaUtil.setCopyOf;
import static java.util.Objects.requireNonNull;

/**
 * Source engine application identifier.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SourceApp {

    private static final Logger L = LogUtils.getLogger();
    public static final SourceApp UNKNOWN = new SourceAppBuilder()
            .setName("Unknown")
            .setAppId(SourceAppID.UNKNOWN)
            .build();

    private final String name;
    private final int appId;
    private final int versionMin;
    private final int versionMax;
    private final Pattern filePattern;
    private final Set<String> entities;
    private final float pointsEntities;
    private final float pointsFilePattern;

    SourceApp(
            String name,
            int appId,
            int versionMin,
            int versionMax,
            Pattern filePattern,
            Set<String> entities,
            float pointsEntities,
            float pointsFilePattern
    ) {
        this.name = requireNonNull(name);
        this.appId = appId;
        this.versionMin = versionMin;
        this.versionMax = versionMax;
        this.filePattern = filePattern;
        this.entities = setCopyOf(entities);
        this.pointsEntities = pointsEntities;
        this.pointsFilePattern = pointsFilePattern;
    }

    public String getName() {
        return name;
    }

    public int getAppId() {
        return appId;
    }

    public int getVersionMin() {
        return versionMin;
    }

    public int getVersionMax() {
        return versionMax;
    }

    public Pattern getFilePattern() {
        return filePattern;
    }

    public Set<String> getEntities() {
        return entities;
    }

    public float getPointsEntities() {
        return pointsEntities;
    }

    public float getPointsFilePattern() {
        return pointsFilePattern;
    }

    public URI getSteamStoreURI() {
        // don't return the URI for unknown or custom appIDs
        if (this == SourceApp.UNKNOWN || appId < 0) {
            return null;
        }

        try {
            return new URI(String.format("http://store.steampowered.com/app/%d/", appId));
        } catch (URISyntaxException ex) {
            L.log(Level.WARNING, "", ex);
            // this really shouldn't happen...
            return null;
        }
    }

    /**
     * @param name BSP file name to check
     * @return empty optional if can't check name otherwise true if name matches
     */
    public Optional<Boolean> checkName(String name) {
        if (filePattern == null)
            return Optional.empty();
        else
            return Optional.of(filePattern.matcher(name.toLowerCase(Locale.ROOT)).find());
    }

    /**
     * Checks if a BSP version number is valid for this app.
     * 
     * @param bspVersion BSP version number to check
     * @return empty optional if can't check version otherwise true if the version is valid for this app
     */
    public Optional<Boolean> checkVersion(int bspVersion) {
        if (versionMin == -1 && versionMax == -1) {
            return Optional.empty();
        } else {
            return Optional.of((versionMin == -1 || bspVersion >= versionMin)
                    && (versionMax == -1 || bspVersion <= versionMax));
        }
    }

    /**
     * @param classNames
     * @return the percentage of matched entity class names
     */
    public Optional<Float> checkEntities(Set<String> classNames) {
        if (entities.isEmpty())
            return Optional.empty();

        long matches = classNames.stream()
                .filter(entities::contains)
                .peek(s -> L.log(Level.FINER, "Entity match: {0}", s))
                .count();

        return Optional.of(matches / (float) entities.size());
    }

    @Override
    public String toString() {
        return "SourceApp{"
                + "name='" + name + '\''
                + ", appId=" + appId
                + ", versionMin=" + versionMin
                + ", versionMax=" + versionMax
                + ", filePattern=" + filePattern
                + ", entities=" + entities
                + ", pointsEntities=" + pointsEntities
                + ", pointsFilePattern=" + pointsFilePattern
                + '}';
    }
}
