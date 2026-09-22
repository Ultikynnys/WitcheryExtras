package alkalus.main.core;

/**
 * Duck interface mixed onto ExtendedPlayer by ExtendedPlayerUpgradesMixin; access the
 * player's earned WitcheryExtras upgrade flags through it.
 */
public interface WitcheryUpgradeHelper {

    int WE_MIN_LEVEL_FOR_UPGRADES = 10;

    boolean witcheryExtras$isTwilight();

    boolean witcheryExtras$isBloodMagic();

    boolean witcheryExtras$isWereman();

    void witcheryExtras$setTwilight(boolean value);

    void witcheryExtras$setBloodMagic(boolean value);

    void witcheryExtras$setWereman(boolean value);
}
