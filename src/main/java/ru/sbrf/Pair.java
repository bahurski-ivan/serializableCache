package ru.sbrf;

/**
 * Created by Ivan on 11/11/2016.
 */
class Pair<V, T> {
    private V first;
    private T second;

    Pair() {
    }

    Pair(V first, T second) {
        this.first = first;
        this.second = second;
    }

    static <V, T> Pair<V, T> of(V first, T second) {
        return new Pair<V, T>(first, second);
    }

    V getFirst() {
        return first;
    }

    void setFirst(V first) {
        this.first = first;
    }

    T getSecond() {
        return second;
    }

    void setSecond(T second) {
        this.second = second;
    }
}
