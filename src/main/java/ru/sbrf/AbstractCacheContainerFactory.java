package ru.sbrf;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 11/11/2016.
 */
abstract class AbstractCacheContainerFactory {
    Map<CacheType, ThrowableBiFunction<Method, Cache, CacheContainer, IOException>> creatorsMap
            = new HashMap<>();

    AbstractCacheContainerFactory() {
        creatorsMap.put(CacheType.FILE, this::fileContainerCreator);
        creatorsMap.put(CacheType.MEMORY, this::memoryContainerCreator);
        creatorsMap.put(CacheType.MEMORY_AND_FILE, this::fileAndMemoryContainerCreator);
    }

    CacheContainer of(Method method) throws IOException {
        if (method.isAnnotationPresent(Cache.class)) {
            Cache cacheInfo = method.getDeclaredAnnotation(Cache.class);
            ThrowableBiFunction<Method, Cache, CacheContainer, IOException> creator = creatorsMap.get(cacheInfo.type());
            return creator.apply(method, cacheInfo);
        }
        return null;
    }


    abstract CacheContainer createFileContainer(Method method, Cache cacheInfo) throws IOException;

    abstract CacheContainer createMemoryContainer(Method method, Cache cacheInfo) throws IOException;


    private CacheContainer fileContainerCreator(Method method, Cache cacheInfo) throws IOException {
        boolean serializable =
                Arrays.stream(method.getParameters())
                        .allMatch(p -> checkClassSerializability(p.getType()))
                        && checkClassSerializability(method.getReturnType());

        return serializable ? createFileContainer(method, cacheInfo) : null;
    }

    private CacheContainer memoryContainerCreator(Method method, Cache cacheInfo) throws IOException {
        return createMemoryContainer(method, cacheInfo);
    }

    private CacheContainer fileAndMemoryContainerCreator(Method method, Cache cacheInfo) throws IOException {
        CacheContainer fileContainer = fileContainerCreator(method, cacheInfo);
        CacheContainer memoryContainer = memoryContainerCreator(method, cacheInfo);

        return (fileContainer == null) ? memoryContainer :
                new FileAndMemoryCacheContainer(fileContainer, memoryContainer);
    }


    private boolean checkClassSerializability(Class<?> clazz) {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }

    private interface ThrowableBiFunction<T, V, R, E extends Exception> {
        R apply(T t, V v) throws E;
    }
}
