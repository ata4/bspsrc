/*
 ** 2012 Februar 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.lib.app;

import info.ata4.bspsrc.lib.app.definitions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Source engine application database handler.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SourceAppDB {

    private static final Logger L = LogManager.getLogger();
    private static SourceAppDB instance;

    private final List<SourceApp> appList = Arrays.asList(
            AlienSwarmDef.APP,
            BlackMesaDef.APP,
            BladeSymphonyDef.APP,
            BloodyGoodTimeDef.APP,
            ContagionDef.APP,
            CounterStrikeGlobalOffensiveDef.APP,
            CounterStrikeSourceDef.APP,
            CyberDiverDef.APP,
            DarkMessiahOfMightMagicDef.APP,
            DayOfDefeatSourceDef.APP,
            DearEstherDef.APP,
            Dota2Def.APP,
            GarrysModDef.APP,
            HalfLife2DeathmatchDef.APP,
            HalfLife2Def.APP,
            HalfLife2EpisodeOneDef.APP,
            HalfLife2EpisodeTwoDef.APP,
            HalfLifeSourceDef.APP,
            InsurgencyDef.APP,
            Left4Dead2Def.APP,
            Left4DeadDef.APP,
            NoMoreRoomInHellDef.APP,
            Portal2Def.APP,
            PortalDef.APP,
            Postal3Def.APP,
            SiNEpisodesEmergenceDef.APP,
            SynergyDef.APP,
            TacticalInterventionDef.APP,
            TeamFortress2Def.APP,
            TheShipDef.APP,
            TitanfallDef.APP,
            VampireTheMasqueradeBloodlinesDef.APP,
            VindictusMabinogiHeroesDef.APP,
            ZenoClashDef.APP,
            ZombiePanicSourceDef.APP
    );

    public static SourceAppDB getInstance() {
        if (instance == null) {
            instance = new SourceAppDB();
        }

        return instance;
    }

    /**
     * Tries to find the AppID though a heuristical search with the given
     * parameters.
     *
     * @param bspName BSP file name
     * @param bspVersion BSP file version
     * @param classNames Complete set of entity class names
     * @return
     */
    public int find(String bspName, int bspVersion, Set<String> classNames) {
        class SourceAppScore {
            public final SourceApp app;
            public final float score;

            SourceAppScore(SourceApp app, float score) {
                this.app = requireNonNull(app);
                this.score = score;
            }
        }

        return appList.stream()
                .map(app -> new SourceAppScore(app, calculateAppScore(app, bspName, bspVersion, classNames)))
                .peek(appScore -> L.debug(String.format("App %s has score %f", appScore.app.getName(), appScore.score)))
                .max(Comparator.comparing(appScore -> appScore.score))
                .filter(appScore -> appScore.score > 0)
                .map(appScore -> appScore.app)
                .map(SourceApp::getAppId)
                .orElse(SourceAppId.UNKNOWN);
    }

    private float calculateAppScore(SourceApp app, String bspName, int bspVersion, Set<String> classNames) {
        return (app.checkVersion(bspVersion).orElse(true) ? 0 : Float.NEGATIVE_INFINITY)
                + (app.checkEntities(classNames).orElse(0f) * app.getPointsEntities())
                + (app.checkName(bspName).orElse(false) ? app.getPointsFilePattern() : 0);
    }

    public Optional<String> getName(int appId) {
        return appList.stream()
                .filter(app -> app.getAppId() == appId)
                .findAny()
                .map(SourceApp::getName);
    }

    /**
     * @return a map of app IDs to game names
     */
    public Map<Integer, String> getAppList() {
        return appList.stream()
                .collect(Collectors.toMap(SourceApp::getAppId, SourceApp::getName));
    }

    public static URI getSteamStoreURI(int appId) {
        // don't return the URI for unknown or custom appIDs
        if (appId <= 0) {
            return null;
        }

        try {
            return new URI(String.format("http://store.steampowered.com/app/%d/", appId));
        } catch (URISyntaxException ex) {
            L.warn("", ex);
            // this really shouldn't happen...
            return null;
        }
    }
}
