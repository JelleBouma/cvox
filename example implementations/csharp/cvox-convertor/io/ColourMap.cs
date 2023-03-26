using static cvox_convertor.utils.Extensions;
using static cvox_convertor.utils.NumberUtilities;
using System.Drawing;

namespace cvox_convertor.io
{
    public class ColourMap
    {
        List<Color> Order = new();
        Dictionary<Color, int> Counts = new();
        Color current;
        int colourPointer = 0;
        int countPointer = 0;
        int countTotal = 0;
        bool EndReached = false;

        public void Add(Color colour, int count)
        {
            Order.Add(colour);
            Counts.Add(colour, count);
        }

        public List<Color> GetUnused()
        {
            List<Color> res = new();
            foreach (Color colour in Counts.Keys)
                if (Counts[colour] == 0)
                    res.Add(colour);
            return res;
        }

        /**
         * @return Next colour if possible, null otherwise.
         */
        public Color? GetNext()
        {
            if (countPointer == countTotal)
            {
                countPointer = 0;
                GetNew();
            }
            countPointer++;
            return EndReached ? null : current;
        }

        public byte[] ToBytes()
        {
            byte[] res = new byte[Order.Count * 7];
            for (int cc = 0; cc < Order.Count; cc++)
            {
                Color colour = Order[cc];
                Array.Copy(IntsToBytes(colour.ToRgba()), 0, res, cc * 7, 4);
                Array.Copy(IntsToBytes(3, true, Counts[colour]), 0, res, cc * 7 + 4, 3);
            }
            return res;
        }

        /**
         * Try to advance the current colour.
         */
        private void GetNew()
        {
            if (colourPointer < Order.Count)
            {
                current = Order[colourPointer];
                countTotal = Counts[current];
                colourPointer++;
            }
            else
                EndReached = true;
        }

    }
}
