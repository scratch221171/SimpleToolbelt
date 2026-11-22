package net.scratch221171.simpletoolbelt.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.scratch221171.simpletoolbelt.client.keymap.ToolbeltKeyMap;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.config.ClientConfig;

public class ToolbeltUsageHistory {

    private static final Map<InputConstants.Key, Double> KEYS = new LinkedHashMap<>();

    public static void add(InputConstants.Key key) {
        KEYS.replaceAll((k, v) -> v * ClientConfig.Appearance.Hud.DAMPING_RATIO.getAsDouble());
        KEYS.merge(key, 1.0, Double::sum);
    }

    public static List<InputConstants.Key> getRecentKeys(int n, Predicate<InputConstants.Key> valid) {
        Set<InputConstants.Key> top = KEYS.entrySet().stream()
                .filter(e -> valid.test(e.getKey()))
                .sorted(Map.Entry.<InputConstants.Key, Double>comparingByValue()
                        .reversed()
                        .thenComparing(e -> e.getKey().getName()))
                .limit(Math.max(0, n))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return KEYS.keySet().stream().filter(top::contains).toList();
    }

    public static void refresh() {
        KEYS.entrySet().removeIf(entry -> ToolbeltKeyMap.get(entry.getKey())
                .map(ref -> ref.slot() == SelectBeltSlotPayload.STOW_INDEX)
                .orElse(true));
    }

    public static void clear() {
        KEYS.clear();
    }
}
