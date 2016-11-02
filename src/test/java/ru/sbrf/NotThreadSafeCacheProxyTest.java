package ru.sbrf;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Ivan on 16/10/16.
 */
public class NotThreadSafeCacheProxyTest {
    @Test
    public void testSerializable() throws Exception {
        Test1 test = CachedObjectFactory.createCached(Test1.class, CacheProxy::notThreadSafeCacheProxy);

        double result = test.superHardCalculations(100.);

        for (int i = 0; i < 1000; ++i)
            Assert.assertTrue(test.superHardCalculations(100.) == result);
        Assert.assertFalse(test.superHardCalculations(120.) == result);

        Assert.assertTrue(test.nonCachedCalculations(200.) != result);

        test.superHardCalculations(300.);
        test.superHardCalculations(400.);
    }

    @Test
    public void testNotSerializable() throws Exception {
        Test2 test = CachedObjectFactory.createCached(Test2.class, CacheProxy::notThreadSafeCacheProxy);

        test.doSomething(100.);
        test.doSomething(100.);
        test.doSomething(300.);
    }
}