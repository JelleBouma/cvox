using System.Drawing;

namespace cvox_convertor.voxel
{
    public class Palette
    {
        public static readonly int SIZE = 255;
        public Color[] palette = new Color[SIZE];

        public Palette()
        {
            for (int cc = 0; cc < SIZE; cc++)
                palette[cc] = Color.FromArgb(255, 170, 170, 170);
        }

        public Palette(byte[] paletteBytes)
        {
            for (int cc = 0; cc < SIZE; cc++)
                palette[cc] = Color.FromArgb(paletteBytes[cc * 4 + 3], paletteBytes[cc * 4], paletteBytes[cc * 4 + 1], paletteBytes[cc * 4 + 2]);
        }

        public Color getColour(int i)
        {
            return palette[i - 1];
        }
        public void setColour(int i, Color colour)
        {
            palette[i - 1] = colour;
        }
        public Color[] getArray()
        {
            return palette;
        }
    }
}
