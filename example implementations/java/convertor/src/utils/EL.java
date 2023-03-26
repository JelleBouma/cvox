package utils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Extended functionality for ArrayList.
 * @param <E> The type of the elements.
 */
public class EL <E> extends ArrayList<E> {

    public EL(E... varargs) {
        super();
        addAll(varargs);
    }

    /**
     * Get the first element from the list without changing the list.
     * @return the first element in this list.
     */
    public E first() {
        return get(0);
    }

    /**
     * A filter which does not modify this list, but returns a new list where each element is the result of a specified function.
     * The element at index x (ranged from 0 to list size - 1) in the returned list will be the result of the convertor function applied on element x in this list.
     * @param convertor The convertor function which will be applied on each element in this list.
     * @param <T> The return type, can be any type.
     * @return A new list where each element is converted.
     */
    public <T> EL<T> convertAll(Function<E, T> convertor) {
        return stream().map(convertor).collect(Collectors.toCollection(EL<T>::new));
    }

    public <K> Groups<K, E> groupBy(Function<E, K> selector) {
        Groups<K, E> res = new Groups<>();
        for (E el : this)
            res.add(selector, el);
        return res;
    }

    /**
     * A filter which does not modify this list.
     * @param predicate the predicate to check.
     * @return a new list with only the elements for which the predicate is true.
     */
    public EL<E> filter(Predicate<E> predicate) {
        return filter(predicate, EL<E>::new);
    }

    /**
     * A filter which does not modify this list.
     * @param predicate the predicate to check.
     * @return a new list with only the elements for which the predicate is true.
     */
    public EL<E> filter(Predicate<E> predicate, Supplier<EL<E>> supplier) {
        EL<E> res = supplier.get();
        for (E e : this)
            if (predicate.test(e))
                res.add(e);
        return res;
    }

    /**
     * Checks if any element returns true for the predicate.
     * @param predicate the predicate to check.
     * @return true if there is an element that returns true for the predicate, false otherwise.
     */
    public boolean anyMatch(Predicate<E> predicate) {
        for (E e : this)
            if (predicate.test(e))
                return true;
        return false;
    }


    public void addAll(E... varargs) {
        addAll(Arrays.asList(varargs));
    }

    /**
     * @param predicate The predicate to check for.
     * @return The first element (counting up from index 0) for which the predicate evaluates to true.
     * If the predicate is true for no element, null is returned instead.
     */
    public E firstMatch(Predicate<E> predicate) {
        for (E e : this)
            if (predicate.test(e))
                return e;
        return null;
    }

    /**
     * @param predicate The predicate to check for.
     * @return A new list containing all and only elements for which the predicate evaluates to true.
     * If the predicate is true for no element, an empty list is returned.
     */
    public EL<E> allMatches(Predicate<E> predicate) {
        EL<E> res = new EL<>();
        for (E e : this)
            if (predicate.test(e))
                res.add(e);
        return res;
    }

    @Override
    public EL<E> subList(int from, int to) {
        EL<E> res = new EL<>();
        res.addAll(super.subList(from, to));
        return res;
    }

}
