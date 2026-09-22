package alkalus.main.handlers;

import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;

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
 * </ul>
 */
public class VampireTweaksHandler {

    public static final VampireTweaksHandler INSTANCE = new VampireTweaksHandler();

    private static final Logger LOG = LogManager.getLogger("WitcheryExtras");

    private static final int BLOOD_PER_FOOD_VANILLA = 5;
    private static final int BLOOD_PER_FOOD_CREDIT = BLOOD_PER_FOOD_VANILLA - 1;
    private static final int REGEN_INTERVAL_TICKS = 100;
    private static final int REGEN_BONUS_PER_LEVEL = 2;

    private final WeakHashMap<EntityPlayer, int[]> state = new WeakHashMap<>();

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
