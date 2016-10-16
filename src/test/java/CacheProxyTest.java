import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Proxy;

/**
 * Created by Ivan on 16/10/16.
 */

interface TestCalc {
    @Cache
    double superHardCalculations(double seed);

    double nonCachedCalculations(double seed);
}

class TestImpl implements TestCalc {
    @Cache
    public double superHardCalculations(double seed) {
        for (int i = 0; i < 1000000; ++i)
            seed = seed + Math.sin(i % 180) + Math.pow(seed, Math.cos(seed % 60));
        return seed;
    }

    public double nonCachedCalculations(double seed) {
        return seed + seed;
    }
}


public class CacheProxyTest {

    @Test
    public void testIfWorks() throws Exception {
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
    }

    //@Ignore
    @Test
    public void testSerialization() throws Exception {
        TestCalc test = (TestCalc)
                Proxy.newProxyInstance(
                        ClassLoader.getSystemClassLoader(),
                        new Class[]{TestCalc.class},
                        new CacheProxy(new TestImpl())
                );

        test.superHardCalculations(100.);
        test.superHardCalculations(102.);

        try (
                OutputStream file = new FileOutputStream("test.ser");
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(test);
        }
    }

    //@Ignore
    @Test
    public void testDeserialization() throws Exception {
        Object o;

        try (
                InputStream file = new FileInputStream("test.ser");
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer)
        ) {
            o = input.readObject();
        }

        TestCalc test = TestCalc.class.cast(o);

        test.superHardCalculations(100.);
        test.superHardCalculations(102.);
    }
}