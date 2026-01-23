package me.index;

import me.index.algo.Regression;
import me.index.algo.Splittable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTests {
    public final int SEED = 237;
    public final int SIZE = (int) 1e7;

    public int split_test_common(Splittable splittable, List<Long> keys, int maxErr, boolean strict) {
        Holder<Integer> last = new Holder<>(0);
        Holder<Integer> cnt = new Holder<>(0);
        splittable.split(keys, maxErr, (start, end, lrm) -> {
            cnt.v = cnt.v + 1;
            assertEquals(last.v, start, "(start == 0)");
            last.v = end;
            int err = 0;
            for (int i = start; i < end; i++) {
                int p_pos = Math.max(0, (int) (lrm.k() * keys.get(i) + lrm.b()));
                int a_pos = i - start;
                err = Math.max(err, Math.abs(p_pos - a_pos));
            }
            assertTrue(err <= lrm.maxErr(), String.format("(err %d <= lrm.maxErr() %d)", err, lrm.maxErr()));
            if (strict)
                assertTrue(err <= maxErr, String.format("(err %d <= maxErr %d)", err, maxErr));
        });
        assertEquals(last.v, keys.size(), "(end == keys.size())");
        return cnt.v;
    }

    // ---------- split_test weak: ----------

    @Test
    public void split_test_weak_linear() {
        List<Long> L_KEYS_32 = Utils.generateLinearKeys(SIZE, false, new Random(SEED));
        for (int maxErr = 1; maxErr <= 128; maxErr *= 2) {
            System.out.println("[DEBUG] split_test weak linear32, err = " + maxErr + ", segments: " +
                    split_test_common(new Regression(), L_KEYS_32, maxErr, false));
        }
    }

    @Test
    public void split_test_weak_uniform() {
        List<Long> U_KEYS_32 = Utils.generateUniformKeys(SIZE, false, new Random(SEED));
        for (int maxErr = 1; maxErr <= 128; maxErr *= 2) {
            System.out.println("[DEBUG] split_test weak uniform32, err = " + maxErr + ", segments: " +
                    split_test_common(new Regression(), U_KEYS_32, maxErr, false));
        }
    }

    // ----- split_test strict: -----

    @DisabledIfSystemProperty(named = "skip.strict", matches = "true")
    @Test
    public void split_test_strict_linear() {
        List<Long> L_KEYS_64 = Utils.generateLinearKeys(SIZE, true, new Random(SEED));
        for (int maxErr = 1; maxErr <= 128; maxErr *= 2) {
            System.out.println("[DEBUG] split_test strict linear64, err = " + maxErr + ", segments: " +
                    split_test_common(new Regression(), L_KEYS_64, maxErr, true));
        }
    }

    @DisabledIfSystemProperty(named = "skip.strict", matches = "true")
    @Test
    public void split_test_strict_uniform() {
        List<Long> U_KEYS_64 = Utils.generateUniformKeys(SIZE, true, new Random(SEED));
        for (int maxErr = 1; maxErr <= 128; maxErr *= 2) {
            System.out.println("[DEBUG] split_test strict uniform64, err = " + maxErr + ", segments: " +
                    split_test_common(new Regression(), U_KEYS_64, maxErr, true));
        }
    }

}
