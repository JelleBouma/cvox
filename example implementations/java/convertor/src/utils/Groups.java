package utils;

import java.util.function.Function;

public class Groups<K, E> extends EL<Grouping<K, E>>{

    public Grouping<K, E> getGrouping(K key) {
        return firstMatch(g -> g.key == key);
    }

    public void add(Function<E, K> selector, E obj) {
        K key = selector.apply(obj);
        Grouping<K, E> grouping = getGrouping(key);
        if (grouping == null) {
            Grouping<K, E> newGrouping = new Grouping<>(key);
            newGrouping.add(obj);
            add(newGrouping);
        }
        else
            grouping.add(obj);
    }
}
