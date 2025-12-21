package me.index;

import me.index.config.*;
import me.index.map.*;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    // run mask for: [BTree, BTreeAdapt, LIndex, LIndexAdapt]
    public static final String[] MASKS = new String[]{
            "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111",
            "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111"
    };

    // main:
    public static void main(String[] args) {
        if (args.length != 4) {
            throw new RuntimeException("usage: main.jar /path/to/project/ /path/to/sosd/ config run_mask");
        }
        boolean exist = false;
        for (String s : MASKS) {
            if (Objects.equals(args[3], s)) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            throw new RuntimeException("run_mask must be in range 0000...1111");
        }
        Path project_folder = Paths.get(args[0]);
        String config_path = project_folder.resolve("config.properties").toString();
        String result_path = project_folder.getParent().resolve("res.json").toString();
        try (FileWriter writer = new FileWriter(result_path)) {
            String[] results = solve(config_path, args[1], args[3]);
            writer.write("[");
            for (int i = 0; i < results.length; i++) {
                writer.write(results[i]);
                if (i != results.length - 1)
                    writer.write(",");
            }
            writer.write("]");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] solve(String config_path, String sosd_path, String mask) {
        GeneralContext context = GeneralContext.read(config_path, sosd_path);

        long res_t_map = test_t_map(context);
        long res_btree = mask.charAt(0) == '0' ? 0 : test_btree(context);
        long res_btree_adapt = mask.charAt(1) == '0' ? 0 : test_btree_adapt(context);
        long res_l_index = mask.charAt(2) == '0' ? 0 : test_l_index(context);
        long res_l_index_adapt = mask.charAt(3) == '0' ? 0 : test_l_index_adapt(context);

        System.out.println();
        System.out.println("[RESULT] Map: " + res_t_map + " op/s");
        System.out.println("[RESULT] BTree: " + res_btree + " op/s");
        System.out.println("[RESULT] BTreeAdapt: " + res_btree_adapt + " op/s");
        System.out.println("[RESULT] LIndex: " + res_l_index + " op/s");
        System.out.println("[RESULT] LIndexAdapt: " + res_l_index_adapt + " op/s");
        System.out.println();

        return new String[]{
                "\"" + context.cfg.keyset().name() + "\"",
                "\"" + context.cfg.workload().name() + "\"",
                "\"" + context.cfg.workloadPerm().name() + "\"",
                "\"" + context.cfg.workloadFactor().name() + "\"",
                "\"" + context.cfg.dataSize().name() + "\"",
                "\"" + context.cfg.maxErr().name() + "\"",
                Long.toString(res_t_map),
                Long.toString(res_btree),
                Long.toString(res_btree_adapt),
                Long.toString(res_l_index),
                Long.toString(res_l_index_adapt)
        };
    }

    public static long test_t_map(GeneralContext context) {
        Storage storage = new TMap();
        storage.init(context.thin_keys, new ArrayList<>(context.thin_keys), context.cfg.maxErr().value);
        return test_common(storage, context, "T_MAP");
    }

    public static long test_btree(GeneralContext context) {
        Storage storage = new BTree();
        storage.init(context.thin_keys, new ArrayList<>(context.thin_keys), context.cfg.maxErr().value);
        return test_common(storage, context, "BTREE");
    }

    public static long test_btree_adapt(GeneralContext context) {
        Storage storage = new BTreeAdapt();
        storage.init(context.thin_keys, new ArrayList<>(context.thin_keys), context.cfg.maxErr().value);
        return test_common(storage, context, "BTREE_ADAPT");
    }

    public static long test_l_index(GeneralContext context) {
        Storage storage = new LIndex();
        storage.init(context.thin_keys, new ArrayList<>(context.thin_keys), context.cfg.maxErr().value);
        return test_common(storage, context, "L_INDEX");
    }

    public static long test_l_index_adapt(GeneralContext context) {
        Storage storage = new LIndexAdapt();
        storage.init(context.thin_keys, new ArrayList<>(context.thin_keys), context.cfg.maxErr().value);
        return test_common(storage, context, "L_INDEX_ADAPT");
    }

    public static long test_common(Storage storage, GeneralContext context, String name) {
        System.out.println("[DEBUG] ----- gc: -----");
        System.gc();

        System.out.println("[DEBUG][" + name + "] ----- start test: -----");
        int hash = 0;
        long start = System.nanoTime();
        for (int i = 0; i < context.qcount; i++) {
            // 0 - read, 1 - insert, 2 - remove
            if (context.queryTypes[i] == 0) {
                hash += storage.find(context.queries[i], new Holder<>());
            } else if (context.queries[i] == 1) {
                hash += storage.insert(context.queries[i], context.queries[i]);
            } else {
                hash += storage.remove(context.queries[i]);
            }
        }
        long end = System.nanoTime();
        System.out.println("[DEBUG][" + name + "] ----- end test -----");
        if (hash == 42) System.out.println("impossible");

        long totalTime = (end - start) / 1000000L;
        return (((long) context.qcount) * 1000L) / totalTime;
    }
}
