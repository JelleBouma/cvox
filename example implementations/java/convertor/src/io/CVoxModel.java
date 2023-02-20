package io;

import utils.EL;
import voxel.*;

public class CvoxModel extends EL<Cube> {
    public XYZ size;
    public EL<Colour> unusedColours = new EL<>();

    public CvoxModel(XYZ size) {
        this.size = size;
    }

    public CvoxModel(VoxModel voxels) {
        this(voxels.toMatrix());
    }

    public CvoxModel(VoxMatrix matrix) {
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
                        while (cubeExpanded[0] || cubeExpanded[1] || cubeExpanded[2]) // while cube can be expanded
                            for (int ee = 0; ee < 3; ee++) {
                                checkIfSideCanBeExpanded(matrix, cubeExpanded, ee, low, high, i);
                                expandSideIfTrue(cubeExpanded[ee], ee, high);
                            }
                        add(new Cube(low, high, palette[i - 1])); // create the cube
                        for (int xx2 = low.x; xx2 <= high.x; xx2++) // save progress into matrix, so we do not check again for the voxels in this cube.
                            for (int yy2 = low.y; yy2 <= high.y; yy2++)
                                for (int zz2 = low.z; zz2 <= high.z; zz2++)
                                    matrix.content[xx2][yy2][zz2] = 0;
                    }
                }
    }

    private void checkIfSideCanBeExpanded(VoxMatrix matrix, boolean[] cubeExpanded, int side, XYZ low, XYZ high, int i) {
        cubeExpanded[side] &= high.get(side) < size.get(side) - 1;
        if (cubeExpanded[side]) // if x side can be expanded
            for (int ss1 = low.get(side + 1); ss1 <= high.get(side + 1); ss1++) // try to find a voxel that stops x side being expanded
                for (int ss2 = low.get(side + 2); ss2 <= high.get(side + 2); ss2++)
                    if (cubeExpanded[side]) { // if x side can still be expanded
                        XYZ coordinate = new XYZ();
                        coordinate.set(side, high.get(side) + 1);
                        coordinate.set(side + 1, ss1);
                        coordinate.set(side + 2, ss2);
                        cubeExpanded[side] &= (high.x + 1 != size.x) && matrix.get(coordinate) == i;
                    }
                    else
                        return;
    }

    private void expandSideIfTrue(boolean check, int side, XYZ high) {
        if (check)
            high.set(side, high.get(side) + 1);
    }

    public void fill(ColourMap map, EL<Cube> cubes) {
        for (Cube cube : cubes) {
            cube.colour = map.getNext();
            add(cube);
        }
        unusedColours.addAll(map.getUnused());
    }
}
