package ru.sbrf;

/**
 * Created by Ivan on 02/11/2016.
 */
interface Test2 {
    static Test2 instance() {
        return new Test2() {
            private Test1 calc = Test1.instance();

            @Override
            public NotSerializable doSomething(double d) {
                return new NotSerializable(calc.superHardCalculations(d));
            }
        };
    }

    @Cache(type = CacheType.MEMORY, directory = "serialized_cache_objects1/333/222/555")
    NotSerializable doSomething(double d);
}