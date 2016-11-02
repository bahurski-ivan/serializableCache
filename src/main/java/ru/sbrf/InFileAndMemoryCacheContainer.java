package ru.sbrf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Ivan on 28/10/2016.
 */
class InFileAndMemoryCacheContainer implements CacheContainer {
    private final InFileCacheContainer fileCacheContainer;
    private Map<CacheKey, Object> cache;

    private InFileAndMemoryCacheContainer(InFileCacheContainer fileCache) {
        this.fileCacheContainer = fileCache;

        try {
            cache = fileCacheContainer.readCache();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static CacheContainer of(Method method) {
        CacheContainer container = InFileCacheContainer.of(method);

        if (!(container instanceof InFileCacheContainer))
            return new InMemoryCacheContainer();

        return new InFileAndMemoryCacheContainer((InFileCacheContainer) container);
    }

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
        fileCacheContainer.writeCache(cache);
    }
}
