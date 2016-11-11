package ru.sbrf;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Ivan on 16/10/16.
 */
public class NotThreadSafeCacheProxyTest {
    static CacheProxyFactory FACTORY = new CacheProxyFactory(ClassLoader.getSystemClassLoader());

    @Test
    public void testInFile() throws Exception {
        Test1 test = FACTORY.wrap(Test1.instance());

        double result = test.superHardCalculations(100.);

        for (int i = 0; i < 1000; ++i)
            Assert.assertTrue(test.superHardCalculations(100.) == result);
        Assert.assertFalse(test.superHardCalculations(120.) == result);

        Assert.assertTrue(test.nonCachedCalculations(200.) != result);

        test.superHardCalculations(300.);
        test.superHardCalculations(400.);

        System.out.println(test.getInvokeCount());

        Assert.assertTrue(test.getInvokeCount() <= 4);
    }

    @Test
    public void testInFileAndMemory() throws Exception {
        Test2 test = FACTORY.wrap(Test2.instance());

        test.doSomething(100.);
        test.doSomething(100.);
        test.doSomething(300.);

        Assert.assertTrue(test.getInvokeCount() <= 2);
    }


}