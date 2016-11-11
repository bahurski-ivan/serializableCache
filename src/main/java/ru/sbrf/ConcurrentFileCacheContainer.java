package ru.sbrf;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Ivan on 11/11/2016.
 */
class ConcurrentFileCacheContainer extends FileCacheContainer {
    private ConcurrentHashMap<Integer, BucketLock> locks = new ConcurrentHashMap<>();

    public ConcurrentFileCacheContainer(Method method, Cache cacheInfo) throws IOException {
        super(method, cacheInfo);
    }

    @Override
    protected void writeBucket(Path bucketFilePath, List<Map.Entry<CacheKey, Object>> bucket, int bucketHash)
            throws IOException {

        BucketLock bucketLock = locks.computeIfAbsent(bucketHash, (k) -> new BucketLock());
        bucketLock.writeReferenceCount.incrementAndGet();
        Lock writeLock = bucketLock.rwLock.writeLock();

        writeLock.lock();

        super.writeBucket(bucketFilePath, bucket, bucketHash);
        bucketLock.writeReferenceCount.decrementAndGet();
        if (!tryToDeleteLock(bucketHash, bucketLock))
            bucketLock.currentContent = bucket;

        writeLock.unlock();
    }

    @Override
    protected List<Map.Entry<CacheKey, Object>> readBucket(Path bucketFilePath, int bucketHash)
            throws IOException {

        List<Map.Entry<CacheKey, Object>> result;
        BucketLock bucketLock = locks.computeIfAbsent(bucketHash, (k) -> new BucketLock());
        bucketLock.readReferenceCount.incrementAndGet();
        Lock readLock = bucketLock.rwLock.readLock();

        readLock.lock();

        if (bucketLock.currentContent == null) {
            result = super.readBucket(bucketFilePath, bucketHash);
            bucketLock.currentContent = result;
        } else result = bucketLock.currentContent;

        bucketLock.readReferenceCount.decrementAndGet();
        tryToDeleteLock(bucketHash, bucketLock);

        readLock.unlock();

        return result;
    }

    private boolean tryToDeleteLock(int hash, BucketLock bucketLock) {
        int nThreadsWriting = bucketLock.writeReferenceCount.get();
        int nThreadsReading = bucketLock.readReferenceCount.get();
        if (nThreadsReading == 0 && nThreadsWriting == 0) {
            locks.remove(hash);
            return true;
        }
        return false;
    }

    private static class BucketLock {
        final ReentrantReadWriteLock rwLock;
        AtomicInteger writeReferenceCount;
        AtomicInteger readReferenceCount;
        List<Map.Entry<CacheKey, Object>> currentContent;

        BucketLock() {
            this.rwLock = new ReentrantReadWriteLock();
        }
    }
}
