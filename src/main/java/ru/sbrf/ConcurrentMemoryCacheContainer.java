package ru.sbrf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by Ivan on 11/11/2016.
 */
class ConcurrentMemoryCacheContainer implements CacheContainer {
    private final ConcurrentHashMap<CacheKey, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object computeIfAbsent(CacheKey key, Function<CacheKey, Object> mappingFunction) throws IOException {
        return cache.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object remove(CacheKey key) throws IOException {
        return cache.remove(key);
    }

    @Override
    public synchronized void addAll(Collection<Map.Entry<CacheKey, Object>> entries) throws IOException {
        entries.forEach(e -> cache.put(e.getKey(), e.getValue()));
    }

    @Override
    public List<Map.Entry<CacheKey, Object>> toList() throws IOException {
        return new ArrayList<>(cache.entrySet());
    }
}