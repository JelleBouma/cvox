using cvox_convertor.voxel;
using System.Drawing;
using System;

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.io
{
    public class CvoxModel : List<Cube> {
    public XYZ size;
    public List<Color> unusedColours = new();

    public CvoxModel(XYZ size)
    {
        this.size = size;
    }

    public CvoxModel(VoxModel voxels) : this(voxels.toMatrix()) { }

    public CvoxModel(VoxMatrix matrix)
    {
        size = matrix.size;
        Color[] palette = matrix.palette.palette;
        for (int xx = 0; xx < size.X; xx++)
            for (int yy = 0; yy < size.Y; yy++)
                for (int zz = 0; zz < size.Z; zz++)
                { // for every possible voxel location
                    int i = matrix.Content[xx, yy, zz];
                    if (i != 0)
                    {
                        XYZ low = new XYZ(xx, yy, zz); // lowest point of the cube
                        XYZ high = new XYZ(xx, yy, zz); // highest point of the cube
                        bool[] cubeExpanded = { true, true, true };
                        while (cubeExpanded[0] || cubeExpanded[1] || cubeExpanded[2]) // while cube can be expanded
                            for (int ee = 0; ee < 3; ee++)
                            {
                                checkIfSideCanBeExpanded(matrix, cubeExpanded, ee, low, high, i);
                                expandSideIfTrue(cubeExpanded[ee], ee, high);
                            }
                        Add(new Cube(low, high, palette[i - 1])); // create the cube
                        for (int xx2 = low.X; xx2 <= high.X; xx2++) // save progress into matrix, so we do not check again for the voxels in this cube.
                            for (int yy2 = low.Y; yy2 <= high.Y; yy2++)
                                for (int zz2 = low.Z; zz2 <= high.Z; zz2++)
                                    matrix.Content[xx2, yy2, zz2] = 0;
                    }
                }
    }

    private void checkIfSideCanBeExpanded(VoxMatrix matrix, bool[] cubeExpanded, int side, XYZ low, XYZ high, int i)
    {
        cubeExpanded[side] &= high.Get(side) < size.Get(side) - 1;
        if (cubeExpanded[side]) // if x side can be expanded
            for (int ss1 = low.Get(side + 1); ss1 <= high.Get(side + 1); ss1++) // try to find a voxel that stops x side being expanded
                for (int ss2 = low.Get(side + 2); ss2 <= high.Get(side + 2); ss2++)
                    if (cubeExpanded[side])
                    { // if x side can still be expanded
                        XYZ coordinate = new XYZ();
                        coordinate.Set(side, high.Get(side) + 1);
                        coordinate.Set(side + 1, ss1);
                        coordinate.Set(side + 2, ss2);
                        cubeExpanded[side] &= (high.X + 1 != size.X) && matrix.get(coordinate) == i;
                    }
                    else
                        return;
    }

    private void expandSideIfTrue(bool check, int side, XYZ high)
    {
        if (check)
            high.Set(side, high.Get(side) + 1);
    }

    public void fill(ColourMap map, List<Cube> cubes)
    {
        foreach (Cube cube in cubes)
        {
            cube.Colour = map.GetNext() ?? Color.FromArgb(255, 170, 170, 170);
            Add(cube);
        }
        unusedColours.AddRange(map.GetUnused());
    }
}
}
