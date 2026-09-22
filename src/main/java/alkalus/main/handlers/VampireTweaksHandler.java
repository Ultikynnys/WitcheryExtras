package alkalus.main.handlers;

import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.ChatUtil;

import alkalus.main.core.WitcheryUpgradeHelper;
import alkalus.main.core.WitcheryUpgrades;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Vampire quality-of-life rebalance (server side):
 * <ul>
 * <li>Hunger costs 1 blood per food point instead of 5: stock GenericEvents drains 5 blood per restored
 *     food point; we credit 4 back per point so the net cost becomes 1.</li>
 * <li>Passive blood regeneration while fed, scaling with vampire level (Tier reward).</li>
 * <li>Vampire level 11 - "Twilight Vampire": sunlight no longer drains, debuffs or burns them
 *     (see CreatureUtilMixin); instead they periodically sparkle, a nod to a certain saga.</li>
 * <li>Ascension: Lilith, ever the experimenter, has adapted Thaumcraft's art of warding against the sun.
 *     Hand her a Wand Focus: Warding while at vampire level 10 and she weaves its secret into your blood,
 *     raising you to level 11 (see EntityLilithMixin).</li>
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
     * Twilight Vampire: the Twilight upgrade flag, earned at vampire level 10 via Lilith's warding
     * quest. The central gate enforces the level >= 10 floor and excludes hybrids, so the flag
     * alone never activates the perk on a re-infected or mixed-bloodline player.
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
        if (player.ticksExisted % 40 == 0 && isTwilightVampire(player) && player.worldObj.isDaytime() && player.worldObj
            .canBlockSeeTheSky((int) player.posX, (int) (player.posY + player.height + 1), (int) player.posZ)) {
            ParticleEffect.INSTANT_SPELL.send(SoundEffect.NONE, player, 0.4D, 1.0D, 32);
        }
    }

    /**
     * Lilith's warding quest: a level-10 vampire who hands her the Wand Focus: Warding ascends to
     * level 11. Returns true if the interaction was handled (caller must cancel the vanilla
     * enchant-item fallback); the focus is only consumed on success.
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
            ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.lilith.warding.notvampire", new Object[0]);
            return true;
        }
        if (ex.getVampireLevel() < 10) {
            ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.lilith.warding.notready", new Object[0]);
            return true;
        }
        if (ex.getWerewolfLevel() > 0) {
            ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witcheryextras.lilith.warding.hybrid", new Object[0]);
            return true;
        }
        if (held.stackSize <= 1) {
            player.setCurrentItemOrArmor(0, null);
        } else {
            held.stackSize--;
        }
        ((WitcheryUpgradeHelper) ex).witcheryExtras$setTwilight(true);
        ex.setVampireLevel(ex.getVampireLevel() + 1);
        ChatUtil.sendTranslated(EnumChatFormatting.LIGHT_PURPLE, player, "witcheryextras.lilith.warding.ascended", new Object[0]);
        ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_LEVELUP, player, 1.0D, 2.0D, 64);
        LOG.info("WitcheryExtras: {} has become a Twilight Vampire (level {}) via Lilith's warding quest",
            player.getCommandSenderName(), ex.getVampireLevel());
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
}
