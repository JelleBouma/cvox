using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.voxel
{
    public class Cube
    {
        public XYZ Low; //inclusive
        public XYZ High; //inclusive
        public Color Colour;

        public Cube(XYZ low, XYZ high) => (Low, High) = (low, high);

        public Cube(XYZ low, XYZ high, Color colour) => (Low, High, Colour) = (low, high, colour);

        public static bool operator ==(Cube cube0, Cube cube1)
        {
            return cube0.Low == cube1.Low && cube0.High == cube1.High && cube0.Colour.ToArgb() == cube1.Colour.ToArgb();
        }
        public static bool operator !=(Cube cube0, Cube cube1)
        {
            return !(cube0 == cube1);
        }
    }
}
