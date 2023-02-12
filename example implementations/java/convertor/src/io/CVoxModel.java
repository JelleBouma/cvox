package io;

import utils.EL;
import voxel.*;

public class CVoxModel extends EL<Cube> {
    public XYZ size;
    public EL<Colour> unusedColours = new EL<>();

    public CVoxModel(XYZ size) {
        this.size = size;
    }

    public CVoxModel(VoxModel voxels) {
        this(voxels.toMatrix());
    }

    public CVoxModel(VoxelMatrix matrix) {
        size = matrix.size;
        Colour[] palette = matrix.palette.palette;
        for (int xx = 0; xx < size.x; xx++)
            for (int yy = 0; yy < size.y; yy++)
                for (int zz = 0; zz < size.z; zz++) { // for every possible voxel location
                    int i = matrix.content[xx][yy][zz];
                    if (i != 0) {
                        XYZ low = new XYZ(xx, yy, zz); // lowest point of the cube
                        XYZ high = new XYZ(xx, yy, zz); // highest point of the cube
                        boolean[] cubeExpanded = {true, true, true};
                        while (cubeExpanded[0] || cubeExpanded[1] || cubeExpanded[2]) { // while cube can be expanded
                            if (cubeExpanded[0]) // if x side is being expanded
                                for (int yy2 = low.y; yy2 <= high.y; yy2++) // try to find a voxel that stops x side being expanded
                                    for (int zz2 = low.z; zz2 <= high.z; zz2++)
                                        cubeExpanded[0] &= (high.x + 1 != size.x) && matrix.content[high.x + 1][yy2][zz2] == i;
                            if (cubeExpanded[0]) // if x side can be expanded
                                high.x++;
                            if (cubeExpanded[1]) // if y side is being expanded
                                for (int xx2 = low.x; xx2 <= high.x; xx2++) // try to find a voxel that stops y side being expanded
                                    for (int zz2 = low.z; zz2 <= high.z; zz2++)
                                        cubeExpanded[1] &= (high.y + 1 != size.y) && matrix.content[xx2][high.y + 1][zz2] == i;
                            if (cubeExpanded[1]) // if y side can be expanded
                                high.y++;
                            if (cubeExpanded[2]) // if z side is being expanded
                                for (int xx2 = low.x; xx2 <= high.x; xx2++) // try to find a voxel that stops z side being expanded
                                    for (int yy2 = low.y; yy2 <= high.y; yy2++)
                                        cubeExpanded[2] &= (high.z + 1 != size.z) && matrix.content[xx2][yy2][high.z + 1] == i;
                            if (cubeExpanded[2]) // if z side can be expanded
                                high.z++;
                        }
                        add(new Cube(low, high, palette[i - 1])); // create the cube
                        System.out.println("created cube " + low + high + palette[i - 1]);
                        for (int xx2 = low.x; xx2 <= high.x; xx2++) // save progress into matrix, so we do not have to check again for the voxels in this cube.
                            for (int yy2 = low.y; yy2 <= high.y; yy2++)
                                for (int zz2 = low.z; zz2 <= high.z; zz2++)
                                    matrix.content[xx2][yy2][zz2] = 0;
                    }
                }
    }

    public void fill(ColourMap map, EL<Cube> cubes) {
        for (Cube cube : cubes) {
            cube.colour = map.getNext();
            add(cube);
        }
        unusedColours.addAll(map.getUnused());
    }
}
