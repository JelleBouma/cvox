package utils;

import java.util.Collection;
import java.util.function.Function;

public class Grouping<K, E> extends EL<E> {
    public K key;
    public Grouping(K key) {
        super();
        this.key = key;
    }

    public Grouping(K key, Collection<E> collection) {
        super();
        this.key = key;
        addAll(collection);
    }
}