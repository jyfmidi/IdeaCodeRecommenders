package ru.spbau.recommenders.plugin;

import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Osipov Stanislav
 */

public interface Recommendation {

    double getPriority(@NotNull LookupElement lookupElement);

}
