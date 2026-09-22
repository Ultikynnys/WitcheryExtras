package alkalus.main.handlers;

import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;

import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Vampire quality-of-life rebalance (server side):
 * <ul>
 * <li>Hunger costs 1 blood per food point instead of 5: stock GenericEvents drains 5 blood per restored food point; we
 * credit 4 back per point so the net cost becomes 1.</li>
 * <li>Passive blood regeneration while fed, scaling with vampire level (Tier reward).</li>
 * <li>Vampire level 11 - "Twilight Vampire": sunlight no longer drains, debuffs or burns them (see CreatureUtilMixin);
 * instead they periodically sparkle, a nod to a certain saga.</li>
 * <li>Ascension: Lilith, ever the experimenter, has adapted Thaumcraft's art of warding against the sun. Hand her a
 * Wand Focus: Warding while at vampire level 10 and she weaves its secret into your blood, raising you to level 11 (see
 * EntityLilithMixin).</li>
 * </ul>
 * Thaumcraft interaction is reflective so there is no compile dependency.
 */
public class VampireTweaksHandler {

    public static final VampireTweaksHandler INSTANCE = new VampireTweaksHandler();

    private static final Logger LOG = LogManager.getLogger("WitcheryExtras");

    private static final String FOCUS_WARDING_CLASS = "thaumcraft.common.items.wands.foci.ItemFocusWarding";

    private static final int BLOOD_PER_FOOD_VANILLA = 5;
    private static final int BLOOD_PER_FOOD_CREDIT = BLOOD_PER_FOOD_VANILLA - 1;
    private static final int REGEN_INTERVAL_TICKS = 100;
    private static final int REGEN_BONUS_PER_LEVEL = 2;

    private final WeakHashMap<EntityPlayer, int[]> state = new WeakHashMap<>();

    private boolean focusClassMissingLogged = false;

    /**
     * Twilight Vampire: the Twilight upgrade flag, earned at vampire level 10 via Lilith's warding quest. The central
     * gate enforces the level >= 10 floor and excludes hybrids, so the flag alone never activates the perk on a
     * re-infected or mixed-bloodline player.
     */
    public boolean isTwilightVampire(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (!WitcheryUpgrades.canUseVampireUpgrade(player)) {
            return false;
        }
        return ex.getVampireLevel() >= 11 || ((WitcheryUpgradeHelper) ex).witcheryExtras$isTwilight();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.player;
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || !ex.isVampire() || player.capabilities.isCreativeMode) {
            return;
        }

        int[] s = getState(player);

        // Credit back the stock 5-per-food-point drain so the net cost is 1 per food point.
        int food = player.getFoodStats().getFoodLevel();
        if (food > s[1] && ex.getBloodPower() > 0) {
            int gained = food - s[1];
            ex.increaseBloodPower(Math.min(gained * BLOOD_PER_FOOD_CREDIT, BLOOD_PER_FOOD_CREDIT * 20));
        }
        s[1] = food;

        // Passive regen while fed, scaling with level.
        s[0]++;
        if (s[0] >= REGEN_INTERVAL_TICKS) {
            s[0] = 0;
            int max = ex.getMaxBloodPower();
            if (ex.getBloodPower() < max) {
                int amount = 1 + Math.max(0, ex.getVampireLevel() - 1) * REGEN_BONUS_PER_LEVEL / 2;
                ex.increaseBloodPower(Math.min(amount, max - ex.getBloodPower()));
            }
        }

        // Twilight vampires sparkle in direct sunlight instead of suffering.
        if (player.ticksExisted % 40 == 0 && isTwilightVampire(player)
                && player.worldObj.isDaytime()
                && player.worldObj.canBlockSeeTheSky(
                        (int) player.posX,
                        (int) (player.posY + player.height + 1),
                        (int) player.posZ)) {
            ParticleEffect.INSTANT_SPELL.send(SoundEffect.NONE, player, 0.4D, 1.0D, 32);
        }
    }

    /**
     * Lilith's warding quest: a level-10 vampire who hands her the Wand Focus: Warding ascends to level 11. Returns
     * true if the interaction was handled (caller must cancel the vanilla enchant-item fallback); the focus is only
     * consumed on success.
     */
    public boolean tryLilithWardingQuest(EntityPlayer player) {
        if (player.worldObj.isRemote || player.capabilities.isCreativeMode) {
            return false;
        }
        ItemStack held = player.getHeldItem();
        if (held == null || !isFocusWarding(held)) {
            return false;
        }
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || !ex.isVampire()) {
            ChatUtil.sendTranslated(
                    EnumChatFormatting.DARK_PURPLE,
                    player,
                    "witcheryextras.lilith.warding.notvampire",
                    new Object[0]);
            return true;
        }
        if (ex.getVampireLevel() < 10) {
            ChatUtil.sendTranslated(
                    EnumChatFormatting.DARK_PURPLE,
                    player,
                    "witcheryextras.lilith.warding.notready",
                    new Object[0]);
            return true;
        }
        if (ex.getWerewolfLevel() > 0) {
            ChatUtil.sendTranslated(
                    EnumChatFormatting.DARK_PURPLE,
                    player,
                    "witcheryextras.lilith.warding.hybrid",
                    new Object[0]);
            return true;
        }
        if (held.stackSize <= 1) {
            player.setCurrentItemOrArmor(0, null);
        } else {
            held.stackSize--;
        }
        ((WitcheryUpgradeHelper) ex).witcheryExtras$setTwilight(true);
        ex.setVampireLevel(ex.getVampireLevel() + 1);
        ChatUtil.sendTranslated(
                EnumChatFormatting.LIGHT_PURPLE,
                player,
                "witcheryextras.lilith.warding.ascended",
                new Object[0]);
        ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_LEVELUP, player, 1.0D, 2.0D, 64);
        LOG.info(
                "WitcheryExtras: {} has become a Twilight Vampire (level {}) via Lilith's warding quest",
                player.getCommandSenderName(),
                ex.getVampireLevel());
        return true;
    }

    private boolean isFocusWarding(ItemStack stack) {
        try {
            Class<?> focusClass = Class.forName(FOCUS_WARDING_CLASS);
            return focusClass.isInstance(stack.getItem());
        } catch (ClassNotFoundException e) {
            if (!focusClassMissingLogged) {
                focusClassMissingLogged = true;
                LOG.warn("WitcheryExtras: Thaumcraft not found, Lilith's warding quest disabled");
            }
            return false;
        }
    }

    private int[] getState(EntityPlayer player) {
        int[] s = state.get(player);
        if (s == null) {
            s = new int[2];
            state.put(player, s);
        }
        return s;
    }

    /**
     * Twilight vampires sleep in normal beds like everyone else: with sun immunity there is no need to hide, so night
     * turns into day when they sleep. Witchery's handler blocks sleep in beds at night ("Vampires can only sleep during
     * the day"); we run AFTER it at LOW priority and, if it rejected the attempt for a Twilight vampire, re-approve it.
     * Coffins keep working as before.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.worldObj.isRemote || event.result != EntityPlayer.EnumStatus.OTHER_PROBLEM) {
            return;
        }
        if (!isTwilightVampire(player)) {
            return;
        }
        if (player.worldObj.isDaytime()) {
            return;
        }
        event.result = EntityPlayer.EnumStatus.OK;
        ChatUtil.sendTranslated(
                EnumChatFormatting.LIGHT_PURPLE,
                player,
                "witcheryextras.twilight.sleep",
                new Object[0]);
    }

    /**
     * When a Twilight vampire wakes from a normal bed at night, fast-forward time to morning - the mirror of Witchery's
     * coffin wake logic (which rewinds 11000 ticks to day).
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.entityPlayer.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.entityPlayer;
        if (!isTwilightVampire(player) || !player.isPlayerFullyAsleep()) {
            return;
        }
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);
        if (player.worldObj.getBlock(x, y, z) == com.emoniph.witchery.Witchery.Blocks.COFFIN) {
            return;
        }
        net.minecraft.world.World world = player.worldObj;
        long timeOfDay = world.getWorldTime() % 24000L;
        if (timeOfDay >= 12000L && timeOfDay < 23459L) {
            long newTime = world.getWorldTime() - timeOfDay + 23460L;
            world.setWorldTime(newTime);
        }
    }
}
