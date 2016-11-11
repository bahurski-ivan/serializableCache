package ru.sbrf;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Ivan on 28/10/2016.
 */
class CacheKey implements Serializable {
    private final Object[] arguments;

    CacheKey(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKey cacheKey = (CacheKey) o;

        return Arrays.deepEquals(arguments, cacheKey.arguments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arguments);
    }
}
