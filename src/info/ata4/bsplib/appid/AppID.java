/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.appid;

/**
 * Steam AppID enumeration to identify Source engine based games.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum AppID {
    
    // "virtual" AppIDs that don't exist in the Steam store
    UNKNOWN(-1, "Unknown"),
    VINDICTUS(-100, "Vindictus"),
    HALF_LIFE_2_BETA(-220, "Half-Life 2 (Beta)"),
    // Valve's Source engine games
    HALF_LIFE_2(220, "Half-Life 2"),
    COUNTER_STRIKE_SOURCE(240, "Counter-Strike: Source"),
    DAY_OF_DEFEAT_SOURCE(300, "Day of Defeat: Source"),
    HALF_LIFE_2_DM(320, "Half-Life 2: Deathmatch"),
    HALF_LIFE_2_EP1(380, "Half-Life 2: Episode One"),
    PORTAL(400, "Portal"),
    HALF_LIFE_2_EP2(420, "Half-Life 2: Episode Two"),
    TEAM_FORTRESS_2(440, "Team Fortress 2"),
    LEFT_4_DEAD(500, "Left 4 Dead"),
    LEFT_4_DEAD_2(550, "Left 4 Dead 2"),
    DOTA_2(570, "Dota 2"),
    PORTAL_2(620, "Portal 2"),
    ALIEN_SWARM(630, "Alien Swarm"),
    COUNTER_STRIKE_GO(1800, "Counter-Strike: Global Offensive"),
    // other Source engine games
    SIN_EPISODES_EMERGENCE(1300, "SiN Episodes: Emergence"),
    DARK_MESSIAH_SP(2110, "Dark Messiah of M&M (SP)"),
    DARK_MESSIAH_MP(2130, "Dark Messiah of M&M (MP/Demo)"),
    THE_SHIP(2400, "The Ship"),
    BLOODY_GOOD_TIME(2450, "Bloody Good Time"),
    VAMPIRE_BLOODLINES(2600, "Vampire: The Masquerade - Bloodlines"),
    ZOMBIE_PANIC(17500, "Zombie Panic! Source"),
    SYNERGY(17520, "Synergy"),
    ETERNAL_SILENCE(17550, "Eternal Silence"),
    PVKII(17570, "Pirates, Vikings, and Knights II"),
    DYSTOPIA(17580, "Dystopia"),
    ZENO_CLASH(22200, "Zeno Clash");
    
    private final int appID;
    private final String name;

    private AppID(int appID, String name) {
        this.name = name;
        this.appID = appID;
    }

    public static AppID valueOf(int appID) {
        for (AppID app : values()) {
            if (app.appID == appID) {
                return app;
            }
        }
        return UNKNOWN;
    }

    public int getID() {
        return appID;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
