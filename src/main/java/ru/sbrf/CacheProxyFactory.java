package ru.sbrf;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Created by Ivan on 12/11/2016.
 */
public class CacheProxyFactory {
    private final ClassLoader classLoader;
    private Map<Class<?>, Class<?>[]> interfacesCache = new HashMap<>();

    public CacheProxyFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public <T, V extends T> T wrapThreadSafe(V delegate, Class<?>... interfaces) {
        return wrapDelegate(delegate,
                () -> new SimpleCacheProxy(delegate,
                        new ConcurrentHashMap<>(),
                        new ThreadSafeCacheContainerFactory()),
                interfaces);
    }

    public <T, V extends T> T wrap(V delegate, Class<?>... interfaces) {
        return wrapDelegate(delegate,
                () -> new SimpleCacheProxy(delegate,
                        new HashMap<>(),
                        new NotThreadSafeCacheContainerFactory()),
                interfaces);
    }

    @SuppressWarnings("unchecked")
    private <T, V extends T> T wrapDelegate(V delegate,
                                            Supplier<SimpleCacheProxy> proxyCreator,
                                            Class<?>... interfaces) {
        if (Arrays.stream(interfaces).anyMatch(i ->
                !i.isAssignableFrom(delegate.getClass()) || !i.isInterface()))
            throw new IllegalArgumentException();

        if (interfaces.length == 0) {
            interfaces = enumerateInterfaces(delegate.getClass());
        }

        return (T) Proxy.newProxyInstance(classLoader, interfaces, proxyCreator.get());
    }

    private Class<?>[] enumerateInterfaces(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();

        Class<?> superClass = clazz.getSuperclass();

        Set<Class<?>> visited = new HashSet<>();
        Queue<Class<?>> toTravers = new LinkedList<>();
        toTravers.addAll(Arrays.asList(clazz.getInterfaces()));
        if (superClass != null)
            toTravers.add(superClass);

        while (!toTravers.isEmpty()) {
            Class<?> parentClass = toTravers.poll();

            if (!visited.add(parentClass))
                continue;

            if (parentClass.isInterface())
                result.add(parentClass);

            superClass = parentClass.getSuperclass();
            toTravers.addAll(Arrays.asList(parentClass.getInterfaces()));
            if (superClass != null)
                toTravers.add(superClass);
        }

        return result.toArray(new Class<?>[result.size()]);
    }
}
