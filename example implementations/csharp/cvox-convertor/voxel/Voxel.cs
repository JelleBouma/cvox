using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.voxel
{
    public class Voxel : XYZ
    {
        public int I;

        public Voxel(byte x, byte y, byte z, byte i) : this(x & 0xff, y & 0xff, z & 0xff, i & 0xff) { }

        public Voxel(int x, int y, int z, int i) : base(x, y, z)
        {
            I = i;
        }

        public Voxel(XYZ xyz, int i) : this(xyz.X, xyz.Y, xyz.Z, i) { }
    }
}
