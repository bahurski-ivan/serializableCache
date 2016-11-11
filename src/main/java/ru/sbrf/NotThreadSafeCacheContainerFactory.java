package ru.sbrf;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Ivan on 11/11/2016.
 */
class NotThreadSafeCacheContainerFactory extends AbstractCacheContainerFactory {
    @Override
    CacheContainer createFileContainer(Method method, Cache cacheInfo) throws IOException {
        return new FileCacheContainer(method, cacheInfo);
    }

    @Override
    CacheContainer createMemoryContainer(Method method, Cache cacheInfo) throws IOException {
        return new MemoryCacheContainer();
    }
}
