using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.utils
{
    public static class NumberUtilities
    {

        public static int BytesToInt(byte[] bytes)
        {
            return BytesToInt(bytes, false);
        }
        public static int BytesToInt(byte[] bytes, bool littleEndian)
        {
            int swap = BoolToInt(littleEndian) * 3;
            return ((0xFF & bytes[swap]) << 24) | ((0xFF & bytes[Math.Abs(1 - swap)]) << 16) |
                    ((0xFF & bytes[Math.Abs(2 - swap)]) << 8) | (0xFF & bytes[Math.Abs(3 - swap)]);
        }

        public static int BytesToInt(byte[] bytes, bool littleEndian, int size)
        {
            int res = 0;
            int swap = BoolToInt(littleEndian) * (size - 1);
            for (int bb = 0; bb < size; bb++)
                res |= (0xFF & bytes[Math.Abs(bb - swap)]) << ((size - bb - 1) * 8);
            return res;
        }

        /**
         * @return an integer which is 1 closer to "to" than "from", if possible. "to" otherwise.
         */
        public static int Encroach(int from, int to)
        {
            return Encroach(from, to, 1);
        }

        /**
         * @param step should be positive, "from" and "to" will determine the step direction.
         * @return an integer which is "step" closer to "to" than "from", if possible. "to" otherwise.
         */
        public static int Encroach(int from, int to, int step)
        {
            if (from <= to)
                return Math.Min(from + step, to);
            else
                return Math.Max(from - step, to);
        }

        public static byte[] IntsToBytes(params int[] ints)
        {
            return IntsToBytes(false, ints);
        }

        public static byte[] IntsToBytes(bool littleEndian, params int[] ints)
        {
            return IntsToBytes(4, littleEndian, ints);
        }

        public static byte[] IntsToBytes(int size, bool littleEndian, params int[] ints)
        {
            int swap = BoolToInt(!littleEndian) * (size - 1);
            byte[] res = new byte[ints.Length * size];
            for (int ii = 0; ii < ints.Length; ii++)
                for (int ss = 0; ss < size; ss++)
                    res[ii * size + ss] = (byte)((uint)ints[ii] >> 8 * Math.Abs(ss - swap));
            return res;
        }

        public static byte[] IntsToBytes(List<int> ints)
        {
            return IntsToBytes(false, ints);
        }

        public static byte[] IntsToBytes(bool littleEndian, List<int> ints)
        {
            int swap = BoolToInt(littleEndian) * 3;
            byte[] res = new byte[ints.Count * 4];
            for (int ii = 0; ii < ints.Count; ii++)
            {
                res[ii * 4 + swap] = (byte)((uint)ints[ii] >> 24);
                res[ii * 4 + Math.Abs(1 - swap)] = (byte)(ints[ii] >> 16);
                res[ii * 4 + Math.Abs(2 - swap)] = (byte)(ints[ii] >> 8);
                res[ii * 4 + Math.Abs(3 - swap)] = (byte)ints[ii];
            }
            return res;
        }
        public static int BoolToInt(bool input)
        {
            return input ? 1 : 0;
        }

        public static bool IntToBool(int integer)
        {
            return integer > 0;
        }

        public static int sum(int[] ints)
        {
            int res = 0;
            foreach (int i in ints)
                res += i;
            return res;
        }
        public static int sum(List<int> ints)
        {
            int res = 0;
            foreach (int i in ints)
                res += i;
            return res;
        }

    }
}
