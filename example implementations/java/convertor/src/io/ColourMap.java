package io;

import utils.EL;
import voxel.Colour;
import static utils.NumberUtilities.*;

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

    public byte[] toBytes()
    {
        byte[] res = new byte[order.size() * 7];
        for (int cc = 0; cc < order.size(); cc++)
        {
            Colour colour = order.get(cc);
            System.arraycopy(intsToBytes(Colour.argbToRgba(colour.getRGB())), 0, res, cc * 7, 4);
            System.arraycopy(intsToBytes(3, true, counts.get(colour)), 0, res, cc * 7 + 4, 3);
        }
        return res;
    }

}
