package ru.sbrf;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ivan on 02/11/2016.
 */
interface Test1 {
    static Test1 instance() {
        return new Test1() {
            private AtomicInteger counter = new AtomicInteger();

            @Override
            public double superHardCalculations(double seed) {
                counter.incrementAndGet();

                for (int i = 0; i < 1000000; ++i)
                    seed = seed + Math.sin(i % 180) + Math.pow(seed, Math.cos(seed % 60));
                return seed;
            }

            @Override
            public double nonCachedCalculations(double seed) {
                return seed + seed;
            }

            @Override
            public int getInvokeCount() {
                return counter.get();
            }
        };
    }

    @Cache(type = CacheType.FILE, directory = "serialized_cache_objects1/333/222/555")
    double superHardCalculations(double seed);

    double nonCachedCalculations(double seed);

    int getInvokeCount();
}