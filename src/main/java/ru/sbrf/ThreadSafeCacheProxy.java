package ru.sbrf;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Ivan on 02/11/2016.
 */
class ThreadSafeCacheProxy extends NotThreadSafeCacheProxy {
    ThreadSafeCacheProxy(Object delegate) {
        super(delegate);
    }

    @Override
    CacheContainer containerFor(Method method) throws IOException {
        synchronized (this) {
            return super.containerFor(method);
        }
    }

    @Override
    Object resolveOrCalculateAndAddCache(CacheContainer container, Method method, Object[] args) throws Exception {
        synchronized (container) {
            return super.resolveOrCalculateAndAddCache(container, method, args);
        }
    }
}