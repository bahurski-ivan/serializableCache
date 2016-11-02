package ru.sbrf;

import java.io.IOException;

/**
 * Created by Ivan on 28/10/2016.
 */
interface CacheContainer {
    Object get(CacheKey arguments) throws IOException;

    boolean present(CacheKey arguments) throws IOException;

    void put(CacheKey arguments, Object result) throws IOException;
}
