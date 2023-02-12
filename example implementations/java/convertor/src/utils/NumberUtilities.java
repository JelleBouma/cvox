package utils;

import java.util.ArrayList;
import java.util.Collections;

public class NumberUtilities {
    private NumberUtilities(){}

    public static int[] abs(int[] ints) {
        int[] res = new int[ints.length];
        for (int ii = 0; ii < ints.length; ii++)
            res[ii] = Math.abs(ints[ii]);
        return res;
    }
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
     * @see <a href="https://en.wikipedia.org/wiki/GIF">reversible mapping table</a>
     */
    public static EL<Byte> variableWidthBytes(EL<Integer> ints, EL<Integer> breakpoints, int size) {
        EL<Byte> bytes = new EL<>();
        byte current = 0;
        int window = 8;
        int bytePointer = 0;
        Collections.reverse(breakpoints);
        int breakpoint = breakpoints.isEmpty() ? -1 : breakpoints.pop();
        for (int ii = 0; ii < ints.size(); ii++) {
            if (ii == breakpoint) { // check if we need more bits
                size++;
                System.out.println("switching to " + size + " bits at code" + breakpoint + " out of " + ints.size());
                breakpoint = breakpoints.isEmpty() ? -1 : breakpoints.pop();
            }
            if (bytePointer == 0)
                window = 8;
            int varOffset = window - 8;
            int varCounter = 0;
            while (varCounter < size) {
                byte varBits = (byte) (varOffset >= 0 ? ints.get(ii) >>> varOffset : ints.get(ii) << (varOffset * -1));
                current |= varBits;
                bytePointer += window;
                if (bytePointer == 8) {
                    bytes.add(current);
                    current = 0;
                    bytePointer = 0;
                }
                varCounter += window;
                varOffset += varOffset > 0 ? window : 8;
                int spaceLeftInByte = 8 - bytePointer;
                int spaceLeftInVar = varCounter == size ? size : size - varCounter;
                window = Math.min(spaceLeftInByte, spaceLeftInVar);
            }
        }
        if (bytePointer != 0) // add spillover bits if necessary
            bytes.add(current);
        return bytes;
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

    /** Limits an integer so that it is higher than a bottom limit and lower than a top limit.
     * If this is already true for the input, then it is returned unchanged.
     * @param input The integer to be limited.
     * @param bottomLimit The bottom limit, if none is required use Integer.MIN_VALUE.
     * @param topLimit  The top limit, if none is required use Integer.MAX_VALUE.
     * @return A limited integer which is equal to one of the inputs.
     */
    public static int limit(int input, int bottomLimit, int topLimit) {
        return Math.min(Math.max(input, bottomLimit), topLimit);
    }

    public static int min(EL<Integer> input) {
        return input.reduce(Integer.MAX_VALUE, Math::min);
    }

    public static int max(EL<Integer> input) {
        return input.reduce(Integer.MIN_VALUE, Math::max);
    }

    public static int max(int... input) {
        int res = Integer.MIN_VALUE;
        for (int inp : input)
            res = Math.max(res, inp);
        return res;
    }

    public static int min(int... input) {
        int res = Integer.MAX_VALUE;
        for (int inp : input)
            res = Math.min(res, inp);
        return res;
    }

    public static boolean powerOfTwo(int n)
    {
        return (n & n-1)==0;
    }

    private static int gcd(int x, int y) {
        return (y == 0) ? x : gcd(y, x % y);
    }

    public static int gcd(EL<Integer> numbers) {
        return numbers.reduce(0, NumberUtilities::gcd);
    }

    public static int lcm(EL<Integer> numbers) {
        return numbers.reduce(1, (x, y) -> x * (y / gcd(x, y)));
    }

    public static int difference(int x, int y) {
        return Math.abs(x - y);
    }

    /**
     * @return a random number from 0 (inclusive) to limit (exclusive).
     */
    public static int random(int limit) {
        return (int)(Math.random() * limit);
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

    /**
     * Sequentially increments or decrements from a list of integers so that it equals the target.
     * Returns a new object and does not alter the input list.
     */
    public static EL<Integer> equalise(EL<Integer> list, int target) {
        int sum = sum(list);
        EL<Integer> res = list.copy();
        boolean countUp = target - sum > 0;
        int ii = 0;
        for (int difference = Math.abs(target - sum); difference > 0; difference--)
            res.set(ii, res.get(ii) + (countUp ? 1 : -1));
        return res;
    }

}
