package ru.sbrf;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by Ivan on 12/11/2016.
 */
public class ThreadSafeCacheContainerFactoryTest {
    @Test
    public void checkFile1() throws Exception {
        ThreadSafeCacheContainerFactory factory = new ThreadSafeCacheContainerFactory();

        Method m = SimpleInFile.class.getMethod("method");
        CacheContainer container = factory.of(m);
        Assert.assertTrue(container.getClass().equals(ConcurrentFileCacheContainer.class));
    }

    @Test
    public void checkFile2() throws Exception {
        ThreadSafeCacheContainerFactory factory = new ThreadSafeCacheContainerFactory();
        Method m = MustBeNull.class.getMethod("method");
        CacheContainer container = factory.of(m);
        Assert.assertTrue(container == null);
    }

    @Test
    public void checkMemory() throws Exception {
        ThreadSafeCacheContainerFactory factory = new ThreadSafeCacheContainerFactory();
        Method m = MustBeInMemory.class.getMethod("method");
        CacheContainer container = factory.of(m);
        Assert.assertTrue(container.getClass().equals(ConcurrentMemoryCacheContainer.class));
    }

    @Test
    public void checkMemory2() throws Exception {
        ThreadSafeCacheContainerFactory factory = new ThreadSafeCacheContainerFactory();
        Method m = SimpleInMemory.class.getMethod("method");
        CacheContainer container = factory.of(m);
        Assert.assertTrue(container.getClass().equals(ConcurrentMemoryCacheContainer.class));
    }

    @Test
    public void checkMemoryAndFile() throws Exception {
        ThreadSafeCacheContainerFactory factory = new ThreadSafeCacheContainerFactory();
        Method m = SimpleMemoryAndFile.class.getMethod("method");
        CacheContainer container = factory.of(m);
        Assert.assertTrue(container instanceof FileAndMemoryCacheContainer);
    }

    interface MustBeNull {
        @Cache(type = CacheType.FILE)
        NotSerializable method();
    }

    interface SimpleInFile {
        @Cache(type = CacheType.FILE)
        int method();
    }

    interface MustBeInMemory {
        @Cache(type = CacheType.MEMORY_AND_FILE)
        NotSerializable method();
    }

    interface SimpleInMemory {
        @Cache(type = CacheType.MEMORY)
        int method();
    }

    interface SimpleMemoryAndFile {
        @Cache(type = CacheType.MEMORY_AND_FILE)
        int method();
    }
}