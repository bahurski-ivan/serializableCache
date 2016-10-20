import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * Created by Ivan on 16/10/16.
 */

interface TestCalc {
    @Cache(type = CacheType.MEMORY_AND_FILE)
    double superHardCalculations(double seed);

    double nonCachedCalculations(double seed);
}

interface Test2 {
    @Cache(type = CacheType.MEMORY, directory = "serialized_cache_objects1/333/222/555")
    NotSerializable doSometing(double d);
}

class TestImpl implements TestCalc {
    public double superHardCalculations(double seed) {
        for (int i = 0; i < 1000000; ++i)
            seed = seed + Math.sin(i % 180) + Math.pow(seed, Math.cos(seed % 60));
        return seed;
    }

    public double nonCachedCalculations(double seed) {
        return seed + seed;
    }
}

class NotSerializable {

    private final double result;

    public NotSerializable(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }
}

class Test2Impl implements Test2 {
    private TestImpl calc = new TestImpl();

    public NotSerializable doSometing(double d) {
        return new NotSerializable(calc.superHardCalculations(d));
    }
}


public class CacheProxyTest {

    @Test
    public void testSerializable() throws Exception {
        TestCalc test = (TestCalc)
                Proxy.newProxyInstance(
                        ClassLoader.getSystemClassLoader(),
                        new Class[]{TestCalc.class},
                        new CacheProxy(new TestImpl())
                );

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
        Test2 test = (Test2)
                Proxy.newProxyInstance(
                        ClassLoader.getSystemClassLoader(),
                        new Class[]{Test2.class},
                        new CacheProxy(new Test2Impl())
                );

        test.doSometing(100.);
        test.doSometing(100.);
        test.doSometing(300.);
    }
}