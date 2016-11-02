package ru.sbrf;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 05/10/16.
 */
class NotThreadSafeCacheProxy implements CacheProxy {
    private Object delegate;
    private Map<Method, CacheContainer> cache = new HashMap<>();

    NotThreadSafeCacheProxy(Object delegate) {
        this.delegate = delegate;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        CacheContainer container;

        if ((container = containerFor(method)) != null)
            result = resolveOrCalculateAndAddCache(container, method, args);
        else
            result = method.invoke(delegate, args);

        return result;
    }

    CacheContainer containerFor(Method method) throws IOException {
        CacheContainer container = cache.get(method);
        Cache cacheInfo = method.getAnnotation(Cache.class);

        if (container == null && cacheInfo != null) {
            container = cacheInfo.type().createContainer(method);

            if (container != null)
                cache.put(method, container);
        }

        return container;
    }

    Object resolveOrCalculateAndAddCache(CacheContainer container, Method method, Object[] args)
            throws Exception {
        Object result;
        CacheKey key = new CacheKey(args);

        if (!container.present(key)) {
            result = method.invoke(delegate, args);
            container.put(key, result);
        } else
            result = container.get(key);

        return result;
    }
}
