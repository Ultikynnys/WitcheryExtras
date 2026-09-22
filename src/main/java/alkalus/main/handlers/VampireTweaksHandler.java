package alkalus.main.handlers;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emoniph.witchery.common.ExtendedPlayer;
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
 *     (see CreatureUtilMixin); instead they periodically sparkle, a nod to a certain saga.
 *     Reached by drinking Lilith's Blood while at level 10 AND wearing an MV-or-better electric
 *     helmet (IC2 item tier &gt;= 2, the GregTech MV gate); without the helmet the drink just
 *     grants the vanilla +2000 blood as usual.</li>
 * </ul>
 * All mod interaction is reflective so no GregTech/IC2 compile dependency is needed.
 */
public class VampireTweaksHandler {

    public static final VampireTweaksHandler INSTANCE = new VampireTweaksHandler();

    private static final Logger LOG = LogManager.getLogger("WitcheryExtras");

    private static final int BLOOD_PER_FOOD_VANILLA = 5;
    private static final int BLOOD_PER_FOOD_CREDIT = BLOOD_PER_FOOD_VANILLA - 1;
    private static final int REGEN_INTERVAL_TICKS = 100;
    private static final int REGEN_BONUS_PER_LEVEL = 2;

    private final WeakHashMap<EntityPlayer, int[]> state = new WeakHashMap<>();

    private boolean ic2MissingLogged = false;

    /**
     * Twilight Vampire: vampire level 11 ("level 11" is the Twilight tier; see ExtendedPlayerLevelMixin).
     */
    public boolean isTwilightVampire(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || ex.getVampireLevel() < 11 || player.capabilities.isCreativeMode) {
            return false;
        }
        ItemStack helmet = player.inventory.armorItemInSlot(3);
        if (helmet == null) {
            return false;
        }
        Item item = helmet.getItem();
        if (item == null) {
            return false;
        }
        try {
            Class<?> electricItem = Class.forName("ic2.api.item.IElectricItem");
            if (!electricItem.isInstance(item)) {
                return false;
            }
            Method getTier = electricItem.getMethod("getTier");
            Object tier = getTier.invoke(item);
            return tier instanceof Number && ((Number) tier).intValue() >= 2;
        } catch (ClassNotFoundException e) {
            if (!ic2MissingLogged) {
                ic2MissingLogged = true;
                LOG.warn("WitcheryExtras: IC2 API not found, Twilight Vampire gate disabled");
            }
            return false;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOG.warn("WitcheryExtras: Twilight Vampire tier check failed", e);
            return false;
        }
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
     * Grants Twilight tier (level 11) to a level-10 vampire drinking Lilith's Blood while wearing an
     * MV-or-better electric helmet. Returns true if the tier was granted; if false the caller should
     * let the vanilla drink logic proceed (+2000 blood).
     */
    public boolean tryGrantTwilightTier(EntityPlayer player) {
        ExtendedPlayer ex = ExtendedPlayer.get(player);
        if (ex == null || player.worldObj.isRemote || player.capabilities.isCreativeMode) {
            return false;
        }
        if (ex.getVampireLevel() != 10 || !isTwilightHelmetWorn(player)) {
            return false;
        }
        ex.setVampireLevel(11);
        LOG.info("WitcheryExtras: {} has become a Twilight Vampire (level 11)", player.getCommandSenderName());
        ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_LEVELUP, player, 1.0D, 2.0D, 64);
        return true;
    }

    private boolean isTwilightHelmetWorn(EntityPlayer player) {
        ItemStack helmet = player.inventory.armorItemInSlot(3);
        if (helmet == null) {
            return false;
        }
        Item item = helmet.getItem();
        if (item == null) {
            return false;
        }
        try {
            Class<?> electricItem = Class.forName("ic2.api.item.IElectricItem");
            if (!electricItem.isInstance(item)) {
                return false;
            }
            Method getTier = electricItem.getMethod("getTier");
            Object tier = getTier.invoke(item);
            return tier instanceof Number && ((Number) tier).intValue() >= 2;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException | RuntimeException e) {
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
