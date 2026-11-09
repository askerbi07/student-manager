package com.myproject.strategy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Реестр стратегий сортировки. Ключ -> фабрика стратегии.
 * Никакого switch в потребителях: они работают через Function<String, SortStrategy>.
 */
public final class SortStrategyFactory {

    private static final Map<String, Supplier<SortStrategy>> REGISTRY;

    static {
        Map<String, Supplier<SortStrategy>> registry = new LinkedHashMap<>();
        registry.put("group", SortByGroup::new);
        registry.put("gpa", SortByGpa::new);
        registry.put("gradebook", SortByGradeBook::new);
        registry.put("even", EvenValueSortStrategy::new);
        REGISTRY = Collections.unmodifiableMap(registry);
    }

    private SortStrategyFactory() {
    }

    public static SortStrategy getStrategy(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type == null");
        }
        Supplier<SortStrategy> supplier = REGISTRY.get(type.toLowerCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return supplier.get();
    }

    public static Set<String> availableKeys() {
        return REGISTRY.keySet();
    }
}
