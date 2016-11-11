package ru.sbrf;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Ivan on 28/10/2016.
 */
interface CacheContainer {
    Object remove(CacheKey key) throws IOException;

    List<Map.Entry<CacheKey, Object>> toList() throws IOException;

    default void addAll(Collection<Map.Entry<CacheKey, Object>> entries) throws IOException {
        for (Map.Entry<CacheKey, Object> e : entries)
            computeIfAbsent(e.getKey(), (k) -> e.getValue());
    }

    Object computeIfAbsent(CacheKey key, Function<CacheKey, Object> mappingFunction) throws IOException;
}