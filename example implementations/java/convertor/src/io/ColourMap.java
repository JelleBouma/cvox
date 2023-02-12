package io;

import utils.EL;
import voxel.Colour;

import java.util.HashMap;

public class ColourMap {
    EL<Colour> order = new EL<>();
    HashMap<Colour, Integer> counts = new HashMap<>();
    Colour current;
    int colourPointer = 0;
    int countPointer = 0;
    int countTotal = 0;

    void add(Colour colour, int count) {
        order.add(colour);
        counts.put(colour, count);
    }

    EL<Colour> getUnused() {
        EL<Colour> res = new EL<>();
        for (Colour colour : counts.keySet())
            if (counts.get(colour) == 0)
                res.add(colour);
        return res;
    }

    /**
     * @return Next colour if possible, null otherwise.
     */
    Colour getNext() {
        if (countPointer == countTotal) {
            countPointer = 0;
            getNew();
        }
        countPointer++;
        return current;
    }

    /**
     * Try to advance the current colour, set it to null if not possible.
     */
    private void getNew() {
        if (colourPointer < order.size()) {
            current = order.get(colourPointer);
            countTotal = counts.get(current);
            colourPointer++;
        }
        else
            current = null;
    }

}
