using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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
