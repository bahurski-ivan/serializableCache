package ru.sbrf;

/**
 * Created by Ivan on 02/11/2016.
 */
interface Test1 {
    static Test1 instance() {
        return new Test1() {
            @Override
            public double superHardCalculations(double seed) {
                for (int i = 0; i < 1000000; ++i)
                    seed = seed + Math.sin(i % 180) + Math.pow(seed, Math.cos(seed % 60));
                return seed;
            }

            @Override
            public double nonCachedCalculations(double seed) {
                return seed + seed;
            }
        };
    }

    @Cache(type = CacheType.MEMORY, directory = "serialized_cache_objects1/333/222/555")
    double superHardCalculations(double seed);

    double nonCachedCalculations(double seed);
}