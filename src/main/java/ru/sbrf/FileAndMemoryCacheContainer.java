package ru.sbrf;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by Ivan on 28/10/2016.
 */
class FileAndMemoryCacheContainer implements CacheContainer {
    private final CacheContainer memoryCacheContainer;
    private final CacheContainer fileCacheContainer;

    FileAndMemoryCacheContainer(CacheContainer fileCacheContainer,
                                CacheContainer memoryCacheContainer) throws IOException {
        Objects.requireNonNull(fileCacheContainer);
        Objects.requireNonNull(memoryCacheContainer);

        List<Map.Entry<CacheKey, Object>> entries = fileCacheContainer.toList();
        memoryCacheContainer.addAll(entries);

        this.fileCacheContainer = fileCacheContainer;
        this.memoryCacheContainer = memoryCacheContainer;
    }

    @Override
    public Object remove(CacheKey key) throws IOException {
        Object oldValue = memoryCacheContainer.remove(key);
        fileCacheContainer.remove(key);
        return oldValue;
    }

    @Override
    public List<Map.Entry<CacheKey, Object>> toList() throws IOException {
        return memoryCacheContainer.toList();
    }

    @Override
    public Object computeIfAbsent(CacheKey key, Function<CacheKey, Object> mappingFunction) throws IOException {
        Object result = memoryCacheContainer.computeIfAbsent(key, mappingFunction);
        fileCacheContainer.computeIfAbsent(key, k -> result);
        return result;
    }
}
