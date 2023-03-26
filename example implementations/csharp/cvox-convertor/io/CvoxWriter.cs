using cvox_convertor.rifflike;
using cvox_convertor.utils;
using cvox_convertor.voxel;
using System.Drawing;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.io
{
    public class CvoxWriter
    {

        public static void Write(CvoxMultimodel cvoxMultimodel, Stream stream)
        {
            List<Chunk> chunks = new() { new Chunk(CvoxID.CVOX, IntsToBytes(true, 1)) };
            for (int mm = 0; mm < cvoxMultimodel.Models.Count; mm++)
            {
                CvoxModel model = cvoxMultimodel.Models[mm];
                byte[] size = new byte[15];
                size[0] = (byte)model.size.X;
                size[1] = (byte)model.size.Y;
                size[2] = (byte)model.size.Z;
                XYZ translation = cvoxMultimodel.Translations[mm];
                byte[] translationBytes = IntsToBytes(true, translation.X, translation.Y, translation.Z);
                Array.Copy(translationBytes, 0, size, 3, 12);
                chunks.Add(new Chunk(CvoxID.SIZE, size));
                IEnumerable<IGrouping<Color, Cube>> sortedCubes = model.AllMatches(c => c.Low != c.High).GroupBy(c => c.Colour);
                IEnumerable<IGrouping<Color, Cube>> sortedVoxels = model.AllMatches(c => c.Low == c.High).GroupBy(c => c.Colour);
                if (sortedCubes.Any())
                {
                    ColourMap cmap = new();
                    List<XYZ> cubeXYZs = new();
                    foreach (IGrouping<Color, Cube> grouping in sortedCubes)
                    {
                        cmap.Add(grouping.Key, grouping.Count());
                        foreach (Cube cube in grouping)
                        {
                            cubeXYZs.Add(cube.Low);
                            cubeXYZs.Add(cube.High);
                        }
                    }
                    chunks.Add(new Chunk(CvoxID.CMAP, cmap.ToBytes()));
                    chunks.Add(new Chunk(CvoxID.CUBE, XYZsToBytes(cubeXYZs)));
                }
                if (sortedVoxels.Any())
                {
                    ColourMap vmap = new();
                    List<XYZ> voxelXYZs = new();
                    foreach (IGrouping<Color, Cube> grouping in sortedVoxels)
                    {
                        vmap.Add(grouping.Key, grouping.Count());
                        voxelXYZs.AddRange(grouping.ToList().ConvertAll(c => c.Low));
                    }
                    chunks.Add(new Chunk(CvoxID.VMAP, vmap.ToBytes()));
                    chunks.Add(new Chunk(CvoxID.XYZ, XYZsToBytes(voxelXYZs)));
                }
            }
            foreach (Chunk chunk in chunks)
                RiffWriter.WriteNextChunk(stream, chunk);
        }

        private static byte[] XYZsToBytes(List<XYZ> xyzs)
        {
            byte[] res = new byte[xyzs.Count * 3];
            for (int ii = 0; ii < xyzs.Count; ii++)
            {
                res[ii * 3] = (byte)xyzs[ii].X;
                res[ii * 3 + 1] = (byte)xyzs[ii].Y;
                res[ii * 3 + 2] = (byte)xyzs[ii].Z;
            }
            return res;
        }
    }
}
