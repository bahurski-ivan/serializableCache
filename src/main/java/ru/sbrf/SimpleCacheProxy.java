package ru.sbrf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Ivan on 12/11/2016.
 */
class SimpleCacheProxy implements InvocationHandler {
    private final Object delegate;
    private final Map<Method, CacheContainer> containerMap;
    private final AbstractCacheContainerFactory containerFactory;

    public SimpleCacheProxy(Object delegate,
                            Map<Method, CacheContainer> containerMap,
                            AbstractCacheContainerFactory containerFactory) {
        this.delegate = delegate;
        this.containerMap = containerMap;
        this.containerFactory = containerFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        boolean resultCalculated = false;

        CacheContainer container = containerMap.get(method);

        if (container == null) {
            CacheContainer newContainer = containerFactory.of(method);
            if (newContainer != null)
                container = containerMap.computeIfAbsent(method, (k) -> newContainer);
        }

        if (container != null) {
            try {
                result = container.computeIfAbsent(new CacheKey(args), k -> {
                    try {
                        return method.invoke(delegate, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
                resultCalculated = true;
            } catch (RuntimeException e) {
                throw e.getCause();
            }
        }

        return resultCalculated ? result : method.invoke(delegate, args);
    }
}
