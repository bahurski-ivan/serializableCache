package ru.sbrf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Created by Ivan on 20/10/16.
 */
public enum CacheType {
    FILE(InFileCacheContainer::of),
    MEMORY(b -> new InMemoryCacheContainer()),
    MEMORY_AND_FILE(InFileAndMemoryCacheContainer::of);

    private final Function<Method, ? extends CacheContainer> creator;

    CacheType(Function<Method, ? extends CacheContainer> creator) {
        this.creator = creator;
    }

    CacheContainer createContainer(Method method) throws IOException {
        try {
            return creator.apply(method);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}