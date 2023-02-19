package utils;

import java.util.ArrayList;
import java.util.Collections;

public class NumberUtilities {
    private NumberUtilities(){}

    public static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, false);
    }
    public static int bytesToInt(byte[] bytes, boolean littleEndian) {
        int swap = boolToInt(littleEndian) * 3;
        return ((0xFF & bytes[swap]) << 24) | ((0xFF & bytes[Math.abs(1 - swap)]) << 16) |
                ((0xFF & bytes[Math.abs(2 - swap)]) << 8) | (0xFF & bytes[Math.abs(3 - swap)]);
    }

    public static int bytesToInt(byte[] bytes, boolean littleEndian, int size) {
        int res = 0;
        int swap = boolToInt(littleEndian) * (size - 1);
        for (int bb = 0; bb < size; bb++)
            res |= (0xFF & bytes[Math.abs(bb - swap)]) << ((size - bb - 1) * 8);
        return res;
    }

    public static byte[] intsToBytes(int... ints) {
        return intsToBytes(false, ints);
    }

    /**
     * @return an integer which is 1 closer to "to" than "from", if possible. "to" otherwise.
     */
    public static int encroach(int from, int to) {
        return encroach(from, to, 1);
    }

    /**
     * @param step should be positive, "from" and "to" will determine the step direction.
     * @return an integer which is "step" closer to "to" than "from", if possible. "to" otherwise.
     */
    public static int encroach(int from, int to, int step) {
        if (from <= to)
            return Math.min(from + step, to);
        else
            return Math.max(from - step, to);
    }

    public static byte[] intsToBytes(boolean littleEndian, int... ints) {
        return intsToBytes(4, littleEndian, ints);
    }

    public static byte[] intsToBytes(int size, boolean littleEndian, int... ints) {
        int swap = boolToInt(!littleEndian) * (size - 1);
        byte[] res = new byte[ints.length * size];
        for (int ii = 0; ii < ints.length; ii++)
            for (int ss = 0; ss < size; ss++)
                res[ii * size + ss] = (byte)(ints[ii] >>> 8 * Math.abs(ss - swap));
        return res;
    }

    public static byte[] intsToBytes(ArrayList<Integer> ints) {
        return intsToBytes(false, ints);
    }

    public static byte[] intsToBytes(boolean littleEndian, ArrayList<Integer> ints) {
        int swap = boolToInt(littleEndian) * 3;
        byte[] res = new byte[ints.size() * 4];
        for (int ii = 0; ii < ints.size(); ii++) {
            res[ii * 4 + swap] = (byte)(ints.get(ii) >>> 24);
            res[ii * 4 + Math.abs(1 - swap)] = (byte)(ints.get(ii) >>> 16);
            res[ii * 4 + Math.abs(2 - swap)] = (byte)(ints.get(ii) >>> 8);
            res[ii * 4 + Math.abs(3 - swap)] = (byte)((int)ints.get(ii));
        }
        return res;
    }
    public static int boolToInt(boolean bool) {
        return bool ? 1 : 0;
    }

    public static boolean intToBool(int integer) {
        return integer > 0;
    }

    public static int sum(int[] ints) {
        int res = 0;
        for (Integer i : ints)
            res += i;
        return res;
    }
    public static int sum(EL<Integer> ints) {
        int res = 0;
        for (Integer i : ints)
            res += i;
        return res;
    }

}
