package ru.sbrf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 28/10/2016.
 */
class InMemoryCacheContainer implements CacheContainer {
    private Map<CacheKey, Object> cache = new HashMap<>();

    @Override
    public Object get(CacheKey arguments) throws IOException {
        return cache.get(arguments);
    }

    @Override
    public boolean present(CacheKey arguments) throws IOException {
        return cache.containsKey(arguments);
    }

    @Override
    public void put(CacheKey arguments, Object result) throws IOException {
        cache.put(arguments, result);
    }
}
