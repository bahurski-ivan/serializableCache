import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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


    private static void writeCache(String fileName, Map<List<Object>, Object> cache) throws IOException {
        try (
                OutputStream file = new FileOutputStream(fileName);
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(cache);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<List<Object>, Object> readCache(String fileName) throws IOException {
        Object cache;

        try (
                InputStream file = new FileInputStream(fileName);
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer)
        ) {
            cache = input.readObject();
        } catch (ClassNotFoundException | EOFException ignored) {
            return new HashMap<>();
        }

        return (Map<List<Object>, Object>) cache;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        CacheContainer container;

        if (method.isAnnotationPresent(Cache.class) && (container = containerFor(method)) != null) {
            List<Object> arguments = Arrays.asList(args);
            if (!container.present(arguments)) {
                result = method.invoke(delegate, args);
                container.put(arguments, result);
            } else
                result = container.get(arguments);

        } else
            result = method.invoke(delegate, args);

        return result;
    }

    private CacheContainer containerFor(Method method) throws IOException {
        CacheContainer container = cache.get(method);

        if (container == null) {
            boolean serializable =
                    Arrays.stream(method.getParameters())
                            .allMatch(p -> checkClassSerializability(p.getType()))
                            && checkClassSerializability(method.getReturnType());

            container = CacheContainer.of(method, serializable);

            if (container == null)
                return null;

            cache.put(method, container);
        }

        return container;
    }

    private boolean checkClassSerializability(Class<?> clazz) {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }

    private interface CacheContainer {

        static CacheContainer of(Method method, boolean serializable) throws IOException {

            Cache cacheInfo = method.getAnnotation(Cache.class);

            if (!serializable)
                return cacheInfo.type() == CacheType.FILE ? null : new InMemoryCacheContainer();

            switch (cacheInfo.type()) {
                case FILE:
                case MEMORY_AND_FILE:

                    Path path;
                    String fileName = method.getName() + ".ser";

                    if (cacheInfo.directory().equals(""))
                        path = Paths.get(System.getProperty("java.io.tmpdir") + '/' + fileName);
                    else {
                        path = Paths.get(cacheInfo.directory() + '/' + fileName);

                        if (!Files.exists(path.getParent()))
                            Files.createDirectories(path.getParent());
                    }

                    if (!Files.exists(path))
                        Files.createFile(path);

                    return cacheInfo.type() == CacheType.FILE ? new InFileCacheContainer(path.toString()) :
                            new InFileAndMemoryCacheContainer(path.toString());

                case MEMORY:
                    return new InMemoryCacheContainer();
            }

            return null;
        }

        Object get(List<Object> arguments) throws IOException;

        boolean present(List<Object> arguments) throws IOException;

        void put(List<Object> arguments, Object result) throws IOException;
    }

    private static class InMemoryCacheContainer implements CacheContainer {

        private Map<List<Object>, Object> cache = new HashMap<>();

        @Override
        public Object get(List<Object> arguments) throws IOException {
            return cache.get(arguments);
        }

        @Override
        public boolean present(List<Object> arguments) throws IOException {
            return cache.containsKey(arguments);
        }

        @Override
        public void put(List<Object> arguments, Object result) throws IOException {
            cache.put(arguments, result);
        }
    }

    private static class InFileCacheContainer implements CacheContainer {

        private final String fileName;

        private InFileCacheContainer(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public Object get(List<Object> arguments) throws IOException {
            Map<List<Object>, Object> cache = readCache(fileName);
            return cache.get(arguments);
        }

        @Override
        public boolean present(List<Object> arguments) throws IOException {
            Map<List<Object>, Object> cache = readCache(fileName);
            return cache.containsKey(arguments);
        }

        @Override
        public void put(List<Object> arguments, Object result) throws IOException {
            Map<List<Object>, Object> cache = readCache(fileName);
            cache.put(arguments, result);
            writeCache(fileName, cache);
        }
    }

    private static class InFileAndMemoryCacheContainer implements CacheContainer {

        private final String fileName;
        private Map<List<Object>, Object> cache = new HashMap<>();

        public InFileAndMemoryCacheContainer(String fileName) throws IOException {
            this.fileName = fileName;
            cache = readCache(fileName);
        }

        @Override
        public Object get(List<Object> arguments) throws IOException {
            return cache.get(arguments);
        }

        @Override
        public boolean present(List<Object> arguments) throws IOException {
            return cache.containsKey(arguments);
        }

        @Override
        public void put(List<Object> arguments, Object result) throws IOException {
            cache.put(arguments, result);
            writeCache(fileName, cache);
        }
    }
}
