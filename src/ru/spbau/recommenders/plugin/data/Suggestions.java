package ru.spbau.recommenders.plugin.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Pavel Talanov
 */
public final class Suggestions {

    @NotNull
    private final Map<String, Integer> methodNameToUsageCount = new HashMap<String, Integer>();

    public void registerUsage(@NotNull String methodName) {
        //TODO: manage overflows
        Integer usageCount = methodNameToUsageCount.get(methodName);
        if (usageCount == null) {
            methodNameToUsageCount.put(methodName, 1);
        } else {
            methodNameToUsageCount.put(methodName, usageCount + 1);
        }
    }

    @Nullable
    public String getMostUsedSuggestion() {
        if (methodNameToUsageCount.isEmpty()) {
            return null;
        }
        Map.Entry<String, Integer> mostCalledMethod = Collections.max(methodNameToUsageCount.entrySet(), new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue() - o2.getValue();
            }
        });
        return mostCalledMethod.getKey();
    }

    @Override
    public String toString() {
        return methodNameToUsageCount.toString();
    }

    @NotNull
    public Collection<Map.Entry<String, Integer>> toCollection() {
        return methodNameToUsageCount.entrySet();
    }

    @NotNull
    public static Suggestions fromMap(@NotNull Map<String, Integer> data) {
        Suggestions suggestions = new Suggestions();
        suggestions.methodNameToUsageCount.putAll(data);
        return suggestions;
    }
}
