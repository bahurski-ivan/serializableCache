package ru.sbrf;

/**
 * Created by Ivan on 02/11/2016.
 */
interface Test3 {
    static Test3 instance() {
        return new Test3() {
            private Test1 calc = Test1.instance();

            @Override
            public double calculate(double seed) {
                return calc.superHardCalculations(seed);
            }

            @Override
            public int getInvokeCount() {
                return calc.getInvokeCount();
            }
        };
    }

    @Cache
    double calculate(double seed);

    int getInvokeCount();
}
