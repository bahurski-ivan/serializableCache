import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 05/10/16.
 */
public class CacheProxy implements InvocationHandler {
    private Object delegate;

    private Map<Method, CacheContainer> cache = new HashMap<>();

    public CacheProxy(Object delegate) {
        this.delegate = delegate;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        CacheContainer container;

        if (method.isAnnotationPresent(Cache.class) && (container = containerFor(method)) != null) {
            CacheKey key = new CacheKey(args);
            if (!container.present(key)) {
                result = method.invoke(delegate, args);
                container.put(key, result);
            } else
                result = container.get(key);
        } else
            result = method.invoke(delegate, args);

        return result;
    }

    private CacheContainer containerFor(Method method) throws IOException {
        CacheContainer container = cache.get(method);

        if (container == null) {
            Cache cacheInfo = method.getAnnotation(Cache.class);

            container = cacheInfo.type().createContainer(method);

            if (container != null)
                cache.put(method, container);
        }

        return container;
    }
}
