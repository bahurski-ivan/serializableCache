import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 28/10/2016.
 */
class InFileCacheContainer implements CacheContainer {
    private Path cacheFilePath;

    private InFileCacheContainer(Method method, Cache cacheInfo) {
        Path path;
        String fileName = method.toString().replaceAll("<>", "_") + ".ser";

        if (cacheInfo.directory().isEmpty())
            path = Paths.get(System.getProperty("java.io.tmpdir") + '/' + fileName);
        else {
            path = Paths.get(cacheInfo.directory() + '/' + fileName);

            if (!Files.exists(path.getParent()))
                try {
                    Files.createDirectories(path.getParent());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
        }

        if (!Files.exists(path))
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        cacheFilePath = path;
    }

    static CacheContainer of(Method method) {
        CacheContainer result = null;

        if (method.isAnnotationPresent(Cache.class)) {
            Cache cacheInfo = method.getAnnotation(Cache.class);

            boolean serializable =
                    Arrays.stream(method.getParameters())
                            .allMatch(p -> checkClassSerializability(p.getType()))
                            && checkClassSerializability(method.getReturnType());

            result = serializable ? new InFileCacheContainer(method, cacheInfo) : null;
        }

        return result;
    }

    private static boolean checkClassSerializability(Class<?> clazz) {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }

    @Override
    public Object get(CacheKey arguments) throws IOException {
        Map<CacheKey, Object> cache = readCache();
        return cache.get(arguments);
    }

    @Override
    public boolean present(CacheKey arguments) throws IOException {
        Map<CacheKey, Object> cache = readCache();
        return cache.containsKey(arguments);
    }

    @Override
    public void put(CacheKey arguments, Object result) throws IOException {
        Map<CacheKey, Object> cache = readCache();
        cache.put(arguments, result);
        writeCache(cache);
    }

    void writeCache(Map<CacheKey, Object> cache) throws IOException {
        try (
                OutputStream file = new FileOutputStream(cacheFilePath.toFile());
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(cache);
        }
    }

    @SuppressWarnings("unchecked")
    Map<CacheKey, Object> readCache() throws IOException {
        Object cache;

        try (
                InputStream file = new FileInputStream(cacheFilePath.toFile());
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer)
        ) {
            cache = input.readObject();
        } catch (ClassNotFoundException | EOFException ignored) {
            return new HashMap<>();
        }

        return (Map<CacheKey, Object>) cache;
    }
}
