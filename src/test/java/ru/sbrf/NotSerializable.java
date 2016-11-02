package ru.sbrf;

/**
 * Created by Ivan on 02/11/2016.
 */
class NotSerializable {
    private final double result;

    NotSerializable(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }
}