package ru.sbrf;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Ivan on 12/11/2016.
 */
class ThreadSafeCacheContainerFactory extends AbstractCacheContainerFactory {
    @Override
    CacheContainer createFileContainer(Method method, Cache cacheInfo) throws IOException {
        return new ConcurrentFileCacheContainer(method, cacheInfo);
    }

    @Override
    CacheContainer createMemoryContainer(Method method, Cache cacheInfo) throws IOException {
        return new ConcurrentMemoryCacheContainer();
    }
}
