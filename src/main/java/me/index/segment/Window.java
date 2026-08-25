package me.index.segment;

import me.index.algo.TConsumer;
import me.index.math.LRM;
import me.index.math.Maths;

import java.util.List;

public interface Window {
    int init_skip(long key, long err);

    boolean can_expand(long key, long pos, int err);

    double[] get_result();

    static void split(Window w, List<Long> keys, int maxErr, TConsumer<Integer, Integer, LRM> lambda) {
        int start = 0;
        while (start < keys.size()) {
            int i = w.init_skip(keys.get(start), maxErr);
            while (start + i < keys.size() && w.can_expand(keys.get(start + i), i, maxErr)) i++;
            double[] cf = w.get_result();
            int max_err = 0;
            for (int j = start; j < start + i; j++) {
                max_err = Math.max(max_err, Math.abs((j - start) - Maths.predict(cf, keys.get(j))));
            }
            LRM lrm = new LRM(cf[0], cf[1], max_err);
            if (lrm.maxErr() > maxErr) {
                System.err.println("[WARNING] large error due to the limited precision of doubles in LRM, "
                        + "found: " + lrm.maxErr() + ", expected: " + maxErr);
            }
            lambda.accept(start, start + i, lrm);
            start += i;
        }
    }
}
