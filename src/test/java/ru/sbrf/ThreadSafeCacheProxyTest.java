package ru.sbrf;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

/**
 * Created by Ivan on 02/11/2016.
 */
public class ThreadSafeCacheProxyTest {
    @Test
    public void testInMemoryCache() throws Exception {
        Test3 test = NotThreadSafeCacheProxyTest.FACTORY.wrapThreadSafe(Test3.instance());

        AtomicReference<Double> previousResult = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 20; ++i) {
            executor.submit(() -> {
                for (int j = 0; j < 20; ++j) {
                    double result = test.calculate(100.);

                    if (previousResult.get() != null)
                        assertTrue(previousResult.get().equals(result));
                    else
                        previousResult.set(result);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(test.getInvokeCount() <= 1);
    }

    @Test
    public void testInFile() throws Exception {
        Test1 test = NotThreadSafeCacheProxyTest.FACTORY.wrapThreadSafe(Test1.instance());

        AtomicReference<Double> previousResult = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 20; ++i) {
            executor.submit(() -> {
                for (int j = 0; j < 20; ++j) {
                    double result = test.superHardCalculations(100.);

                    if (previousResult.get() != null)
                        assertTrue(previousResult.get().equals(result));
                    else
                        previousResult.set(result);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(test.getInvokeCount() <= 1);
    }

    @Test
    public void testInFileAndMemory() throws Exception {
        Test2 test = NotThreadSafeCacheProxyTest.FACTORY.wrapThreadSafe(Test2.instance());

        AtomicReference<Double> previousResult = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 20; ++i) {
            executor.submit(() -> {
                for (int j = 0; j < 20; ++j) {
                    NotSerializable r = test.doSomething(100.);

                    double result = r.getResult();

                    if (previousResult.get() != null)
                        assertTrue(previousResult.get().equals(result));
                    else
                        previousResult.set(result);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(test.getInvokeCount() <= 1);
    }
}