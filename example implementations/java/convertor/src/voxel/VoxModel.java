package voxel;

import io.CvoxModel;
import io.CvoxMultimodel;
import utils.EL;
import static utils.NumberUtilities.intToBool;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class VoxModel extends EL<Voxel> {

    public XYZ size;
    protected Palette palette = new Palette();

    public VoxModel(int x, int y, int z) {
        size = new XYZ(x, y, z);
    }

    public VoxModel(XYZ size) {
        this.size = size;
    }

    public VoxModel(XYZ size, Palette palette) {
        this(size);
        setPalette(palette);
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
    }

    public Palette getPalette() {
        return palette;
    }

    public VoxModel(CvoxMultimodel cvoxMultimodel) {
        this(cvoxMultimodel.size);
        for (int mm = 0; mm < cvoxMultimodel.models.size(); mm++) {
            CvoxModel cvoxModel = cvoxMultimodel.models.get(mm);
            XYZ translation = cvoxMultimodel.translations.get(mm);
            for (Cube cube : cvoxModel)
                for (int xx = cube.low.x; xx <= cube.high.x; xx++)
                    for (int yy = cube.low.y; yy <= cube.high.y; yy++)
                        for (int zz = cube.low.z; zz <= cube.high.z; zz++) {
                            int i = addColour(cube.colour);
                            add(new Voxel(xx + translation.x, yy + translation.y, zz + translation.z, i));
                        }
            for (Colour unused : cvoxModel.unusedColours)
                addColour(unused);
        }
    }

    /**
     * Merge multiple models into one using only a single palette and having overlapping voxels overwrite each other.
     */
    public static VoxModel simpleMerge (EL<VoxModel> models, HashMap<Integer, XYZ> translations, Palette palette) {
        if (models.size() == 1)
            return models.get(0);
        else {
            XYZ min = XYZ.MAX_VALUE;
            XYZ max = XYZ.MIN_VALUE;
            for (int mm = 0; mm < models.size(); mm++) {
                VoxModel model = models.get(mm);
                XYZ translation = translations.get(mm);
                if (translation == null)
                    translation = new XYZ();
                translation = translation.combine(model.size, (t, s) -> t - s / 2); // uncentre
                translations.put(mm, translation);
                min = min.min(translation);
                max = max.max(model.size.add(translation));
            }
            XYZ mergedSize = min.difference(max);
            VoxModel res = new VoxModel(mergedSize);
            for (int mm = 0; mm < models.size(); mm++) {
                XYZ translation = translations.get(mm);
                if (translation == null)
                    translation = new XYZ();
                for (Voxel voxel : models.get(mm))
                    res.add(new Voxel(voxel.add(translation).subtract(min), voxel.i));
            }
            res.palette = palette;
            return res;
        }
    }

    /**
     * Split the model into multiple models, starting from 0,0,0.
     * @param bounds maximum size of the resulting models
     */
    public VoxModel[][][] simpleSplit(XYZ bounds) {
        XYZ finalBounds = bounds.min(size);
        XYZ slices = finalBounds.combine(size, (b, m) -> (int) Math.ceil((double)m / b));
        VoxModel[][][] res = new VoxModel[slices.x][slices.y][slices.z];
        new XYZ().fromTo(slices, (xyz) -> {
            XYZ isEdge = xyz.combine(slices, (i, s) -> i == s - 1 ? 1 : 0);
            XYZ innerBounds = isEdge.combine(finalBounds, (i, b) -> intToBool(i) ? 0 : b);
            XYZ edgeBounds = size.combine(finalBounds, (s, b) -> s % b == 0 ? b : s % b);
            XYZ compactBounds = isEdge.combine(edgeBounds, (i, b) -> i == 0 ? 0 : b);
            XYZ calculatedBounds = compactBounds.add(innerBounds);
            res[xyz.x][xyz.y][xyz.z] = new VoxModel(calculatedBounds, palette);
        });
        for (Voxel voxel : this) {
            XYZ modelXYZ = voxel.div(finalBounds);
            XYZ locationXYZ = voxel.mod(finalBounds);
            res[modelXYZ.x][modelXYZ.y][modelXYZ.z].add(new Voxel(locationXYZ, voxel.i));
        }
        return res;
    }
    public void fillXYZI(byte[] xyzi) {
        for (int bb = 4; bb < xyzi.length; bb += 4)
            add(new Voxel(xyzi[bb], xyzi[bb + 1], xyzi[bb + 2], xyzi[bb + 3]));
    }

    public boolean add(int x, int y, int z, int i) {
        return add(new Voxel(x, y, z, i));
    }

    public boolean add(int x, int y, int z, Colour c) {
        return add(createVoxel(x, y, z, c));
    }

    public Voxel createVoxel(int x, int y, int z, Colour colour) {
        int index = findColourIndex(colour);
        if (index != -1)
            return new Voxel(x, y, z, index);
        else {
            int unusedIndex = findUnusedColourIndex();
            if (unusedIndex != -1) {
                palette.setColour(unusedIndex, colour);
                return new Voxel(x, y, z, unusedIndex);
            }
            else
                return null;
        }
    }

    public int addColour(Colour colour) {
        for (int cc = 1; cc <= Palette.SIZE; cc++)
            if (palette.getColour(cc).equals(colour))
                return cc;
        int index = findUnusedColourIndex();
        if (index != -1)
            palette.setColour(index, colour);
        return index;
    }

    public int findColourIndex(Colour colour) {
        for (int cc = 1; cc <= Palette.SIZE; cc++)
            if (palette.getColour(cc).equals(colour))
                return cc;
        return -1; // colour not in palette
    }

    public int findUnusedColourIndex() {
        for (int cc = 1; cc < 256; cc++) {
            int colourIndex = cc;
            if (!anyMatch(v -> v.i == colourIndex))
                return cc;
        }
        return -1; // all colour indices in use
    }

    public VoxMatrix toMatrix() {
        int[][][] res = new int[size.x][size.y][size.z];
        for (Voxel voxel : this)
            if (voxel.withinBounds(XYZ.ZERO, size.subtract(XYZ.ONE)))
                res[voxel.x][voxel.y][voxel.z] = voxel.i;
        return new VoxMatrix(res, palette);
    }
}
