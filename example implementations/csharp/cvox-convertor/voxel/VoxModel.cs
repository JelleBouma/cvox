using cvox_convertor.io;
using System.Drawing;
using static cvox_convertor.utils.Extensions;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.voxel
{
    public class VoxModel : List<Voxel>
    {

        public XYZ Size;
        protected Palette palette = new Palette();

        public VoxModel(int x, int y, int z)
        {
            Size = new XYZ(x, y, z);
        }

        public VoxModel(XYZ size)
        {
            Size = size;
        }

        public VoxModel(XYZ size, Palette palette) : this(size)
        {
            SetPalette(palette);
        }

        public void SetPalette(Palette palette)
        {
            this.palette = palette;
        }

        public Palette GetPalette()
        {
            return palette;
        }

        public VoxModel(CvoxMultimodel cvoxMultimodel) : this(cvoxMultimodel.Size)
        {
            for (int mm = 0; mm < cvoxMultimodel.Models.Count; mm++)
            {
                CvoxModel cvoxModel = cvoxMultimodel.Models[mm];
                XYZ translation = cvoxMultimodel.Translations[mm];
                foreach (Cube cube in cvoxModel)
                    for (int xx = cube.Low.X; xx <= cube.High.X; xx++)
                        for (int yy = cube.Low.Y; yy <= cube.High.Y; yy++)
                            for (int zz = cube.Low.Z; zz <= cube.High.Z; zz++)
                            {
                                int i = AddColour(cube.Colour);
                                Add(new Voxel(xx + translation.X, yy + translation.Y, zz + translation.Z, i));
                            }
                foreach (Color unused in cvoxModel.unusedColours)
                    AddColour(unused);
            }
        }

        /**
         * Merge multiple models into one using only a single palette and having overlapping voxels overwrite each other.
         */
        public static VoxModel SimpleMerge(List<VoxModel> models, Dictionary<int, XYZ> translations, Palette palette)
        {
            if (models.Count == 1)
                return models[0];
            else
            {
                XYZ min = XYZ.MAX_VALUE;
                XYZ max = XYZ.MIN_VALUE;
                for (int mm = 0; mm < models.Count; mm++)
                {
                    VoxModel model = models[mm];
                    XYZ translation = translations[mm];
                    translation = translation.Combine(model.Size, (t, s) => t - s / 2); // uncentre
                    translations.Add(mm, translation);
                    min = min.Min(translation);
                    max = max.Max(model.Size + translation);
                }
                XYZ mergedSize = min.Difference(max);
                VoxModel res = new VoxModel(mergedSize);
                for (int mm = 0; mm < models.Count; mm++)
                {
                    XYZ translation = translations[mm];
                    foreach (Voxel voxel in models[mm])
                        res.Add(new Voxel(voxel + translation - min, voxel.I));
                }
                res.palette = palette;
                return res;
            }
        }

        /**
         * Split the model into multiple models, starting from 0,0,0.
         * @param bounds maximum size of the resulting models
         */
        public VoxModel[,,] SimpleSplit(XYZ bounds)
        {
            XYZ finalBounds = bounds.Min(Size);
            XYZ slices = finalBounds.Combine(Size, (b, m) => (int)Math.Round((double)m / b, 0, MidpointRounding.AwayFromZero));
            VoxModel[,,] res = new VoxModel[slices.X, slices.Y, slices.Z];
            new XYZ().FromTo(slices, (xyz) =>
            {
                XYZ isEdge = xyz.Combine(slices, (i, s) => i == s - 1 ? 1 : 0);
                XYZ innerBounds = isEdge.Combine(finalBounds, (i, b) => IntToBool(i) ? 0 : b);
                XYZ edgeBounds = Size.Combine(finalBounds, (s, b) => s % b == 0 ? b : s % b);
                XYZ compactBounds = isEdge.Combine(edgeBounds, (i, b) => i == 0 ? 0 : b);
                XYZ calculatedBounds = compactBounds + innerBounds;
                res[xyz.X, xyz.Y, xyz.Z] = new VoxModel(calculatedBounds, palette);
            });
            foreach (Voxel voxel in this)
            {
                XYZ modelXYZ = voxel / finalBounds;
                XYZ locationXYZ = voxel % finalBounds;
                res[modelXYZ.X, modelXYZ.Y, modelXYZ.Z].Add(new Voxel(locationXYZ, voxel.I));
            }
            return res;
        }
        public void FillXYZI(byte[] xyzi)
        {
            for (int bb = 4; bb < xyzi.Length; bb += 4)
                Add(new Voxel(xyzi[bb], xyzi[bb + 1], xyzi[bb + 2], xyzi[bb + 3]));
        }

        public int AddColour(Color colour)
        {
            for (int cc = 1; cc <= Palette.SIZE; cc++)
                if (palette.getColour(cc).ToArgb() == colour.ToArgb())
                    return cc;
            int index = FindUnusedColourIndex();
            if (index != -1)
                palette.setColour(index, colour);
            return index;
        }

        public int FindUnusedColourIndex()
        {
            for (int cc = 1; cc < 256; cc++)
            {
                int colourIndex = cc;
                if (!this.AnyMatch(v => v.I == colourIndex))
                    return cc;
            }
            return -1; // all colour indices in use
        }

        public VoxMatrix ToMatrix()
        {
            int[,,] res = new int[Size.X, Size.Y, Size.Z];
            foreach (Voxel voxel in this)
                if (voxel.WithinBounds(XYZ.ZERO, Size - 1))
                    res[voxel.X, voxel.Y, voxel.Z] = voxel.I;
            return new VoxMatrix(res, palette);
        }
    }
}
