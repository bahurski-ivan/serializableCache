package ru.sbrf;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

/**
 * Created by Ivan on 02/11/2016.
 */
class CachedObjectFactory {
    @NotNull
    static <T> T createCached(Class<T> clazz, T instance, Function<T, ? extends CacheProxy> proxyCreator) {
        return (T) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{clazz},
                proxyCreator.apply(instance)
        );
    }

    @NotNull
    static <T> T createCached(Class<T> clazz, Function<T, ? extends CacheProxy> proxyCreator) {
        Method creator = null;

        try {
            creator = clazz.getMethod("instance");

            if (creator == null)
                throw new RuntimeException("[CachedObjectFactory] method creator (\"T instance()\") = null");

            return createCached(clazz, (T) creator.invoke(null), proxyCreator);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
