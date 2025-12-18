package me.index;

import me.index.config.*;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public final class GeneralContext {
    public final Config cfg;

    public final List<Long> keys;

    public final int keysSize;
    public final int queryCount = (int) 1e7;

    public GeneralContext(Properties props) {
        cfg = Config.read(props);
        if (!cfg.keyset().isLong && !cfg.keyset().isSOSD && cfg.dataSize().name().equals("_max")) {
            throw new RuntimeException("incorrect properties: max size for integer keys is not available");
        }

        System.out.println("[DEBUG] " + cfg);

        if (cfg.keyset().isSOSD) {
            keys = Utils.read("~/data/" + (cfg.keyset().name()).substring(1), cfg.dataSize().size,
                    cfg.keyset().isLong, cfg.keyset().needShift, cfg.keyset().needPlusOne);
        } else if (cfg.keyset().isUniform) {
            keys = Utils.generateUniformKeys(cfg.dataSize().size, cfg.keyset().isLong, new Random(42));
        } else {
            keys = Utils.generateLinearKeys(cfg.dataSize().size, cfg.keyset().isLong, new Random(42));
        }

        keysSize = keys.size();
    }

    public static GeneralContext read(String name) {
        try (FileInputStream fis = new FileInputStream(name)) {
            Properties props = new Properties();
            props.load(fis);
            return new GeneralContext(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
