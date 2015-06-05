package me.kenzierocks.anagar.functional;

/**
 * Consumes objects of type T.
 *
 * @param <T>
 *            - The type object to consume
 */
public interface Consumer<T> {

    void consume(T obj);

}
