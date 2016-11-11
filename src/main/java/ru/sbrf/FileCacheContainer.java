package ru.sbrf;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Ivan on 28/10/2016.
 */
class FileCacheContainer implements CacheContainer {
    private final Path cacheDir;

    FileCacheContainer(Method method, Cache cacheInfo) throws IOException {
        String cacheDirectory = Integer.toString(method.toString().hashCode());

        Path path = Paths.get(
                (cacheInfo.directory().isEmpty() ?
                        System.getProperty("java.io.tmpdir") :
                        cacheInfo.directory()) +
                        File.separatorChar +
                        cacheDirectory
        );

        Files.createDirectories(path);
        cacheDir = path;
    }

    public List<Map.Entry<CacheKey, Object>> toList() throws IOException {
        try {
            return Files.list(cacheDir)
                    .map(p -> {
                        try {
                            int hash = Integer.parseInt(p.getFileName().toString());
                            return readBucket(p, hash);
                        } catch (IOException | NumberFormatException ignored) {
                            // just skip
                            return null;
                        }
                    })
                    .filter(bucket -> bucket != null)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }


    @Override
    public Object remove(CacheKey key) throws IOException {
        Pair<Path, Integer> bucketInfo = pathOf(key);
        Path bucketFilePath = bucketInfo.getFirst();
        int bucketHash = bucketInfo.getSecond();

        List<Map.Entry<CacheKey, Object>> bucket = readBucket(bucketFilePath, bucketHash);

        Object result = null;
        Iterator<Map.Entry<CacheKey, Object>> it = bucket.iterator();

        while (it.hasNext()) {
            Map.Entry<CacheKey, Object> value = it.next();

            if (value.getKey().equals(key)) {
                it.remove();
                result = value.getValue();
            }
        }

        writeBucket(bucketFilePath, bucket, bucketHash);

        return result;
    }

    @Override
    public Object computeIfAbsent(CacheKey key, Function<CacheKey, Object> mappingFunction) throws IOException {
        Pair<Path, Integer> bucketInfo = pathOf(key);
        Path bucketFilePath = bucketInfo.getFirst();
        int bucketHash = bucketInfo.getSecond();

        List<Map.Entry<CacheKey, Object>> bucket = readBucket(bucketFilePath, bucketHash);

        Object result;

        Optional<Map.Entry<CacheKey, Object>> element = bucket.stream()
                .filter(e -> e.getKey().equals(key))
                .findFirst();

        if (!element.isPresent()) {

            result = mappingFunction.apply(key);
            BucketElement resultElement = new BucketElement(key, result);
            bucket.add(resultElement);
            writeBucket(bucketFilePath, bucket, bucketHash);

        } else
            result = element.get().getValue();

        return result;
    }


    protected void writeBucket(Path bucketFilePath, List<Map.Entry<CacheKey, Object>> bucket, int bucketHash)
            throws IOException {
        Files.createFile(bucketFilePath);
        try (
                OutputStream file = new FileOutputStream(bucketFilePath.toFile());
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(bucket);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Map.Entry<CacheKey, Object>> readBucket(Path bucketFilePath, int bucketHash)
            throws IOException {
        if (!Files.exists(bucketFilePath))
            return new ArrayList<>();

        List<Map.Entry<CacheKey, Object>> bucket = null;

        try (
                InputStream file = new FileInputStream(bucketFilePath.toFile());
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer)
        ) {
            Object object = input.readObject();

            if (List.class.isAssignableFrom(object.getClass())) {
                List list = (List) object;

                int i = 0;
                for (; i < list.size(); ++i)
                    if (!(list.get(i) instanceof BucketElement))
                        break;

                if (i == list.size())
                    bucket = (List) object;
            }
        } catch (ClassNotFoundException | EOFException | FileNotFoundException ignored) {
            bucket = null;
        }

        return bucket == null ? new ArrayList<>() : bucket;
    }


    private Pair<Path, Integer> pathOf(CacheKey key) {
        int hash = key.hashCode();
        String fileName = Integer.toString(hash);
        return Pair.of(
                Paths.get(cacheDir.toString() + File.separatorChar + fileName),
                hash
        );
    }

    private static class BucketElement implements Map.Entry<CacheKey, Object>, Serializable {
        private final CacheKey key;
        private Object result;

        BucketElement(CacheKey key, Object result) {
            this.key = key;
            this.result = result;
        }

        static Map.Entry<CacheKey, Object> of(CacheKey key, Object result) {
            return new BucketElement(key, result);
        }

        @Override
        public CacheKey getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return result;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }
}
