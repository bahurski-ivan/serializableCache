package ru.sbrf;

import java.lang.reflect.InvocationHandler;

/**
 * Created by Ivan on 02/11/2016.
 */
public interface CacheProxy extends InvocationHandler {
    static CacheProxy notThreadSafeCacheProxy(Object delegate) {
        return new NotThreadSafeCacheProxy(delegate);
    }

    static CacheProxy threadSafeCacheProxy(Object delegate) {
        return new ThreadSafeCacheProxy(delegate);
    }
}
