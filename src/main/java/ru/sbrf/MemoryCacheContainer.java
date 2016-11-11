package ru.sbrf;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Created by Ivan on 28/10/2016.
 */
class MemoryCacheContainer implements CacheContainer {
    private Map<CacheKey, Object> cache = new HashMap<>();

    @Override
    public Object remove(CacheKey key) throws IOException {
        return cache.remove(key);
    }

    @Override
    public Object computeIfAbsent(CacheKey key, Function<CacheKey, Object> mappingFunction) {
        return cache.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public void addAll(Collection<Map.Entry<CacheKey, Object>> entries) throws IOException {
        entries.forEach(e -> cache.put(e.getKey(), e.getValue()));
    }

    @Override
    public List<Map.Entry<CacheKey, Object>> toList() throws IOException {
        return new ArrayList<>(cache.entrySet());
    }
}
