using cvox_convertor.rifflike;
using cvox_convertor.voxel;
using static cvox_convertor.utils.NumberUtilities;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using cvox_convertor.utils;

namespace cvox_convertor.io
{
    public class CvoxWriter
    {

        public static void Write(CvoxMultimodel cvoxMultimodel, Stream stream)
        {
            List<Chunk> chunks = new(){new Chunk("CVOX", IntsToBytes(true, 1))};
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
                chunks.Add(new Chunk("SIZE", size));
                List<Cube> voxels = model.AllMatches(c => c.Low == c.High);
                List<Cube> cubes = model.AllMatches(c => c.Low != c.High);
                if (cubes.Count > 0)
                {
                    List<IGrouping<Color, Cube>> sortedCubes = cubes.GroupBy(c => c.Colour).ToList();
                    byte[] cubeBytes = new byte[cubes.Count * 6];
                    byte[] cmap = new byte[sortedCubes.Count * 7];
                    int cubeCounter = 0;
                    for (int cc = 0; cc < sortedCubes.Count; cc++)
                    {
                        List<Cube> sameColourCubes = sortedCubes[cc].ToList();
                        int rgba = sameColourCubes.First().Colour.ToRgba();
                        byte[] amountOfCubes = IntsToBytes(3, true, sameColourCubes.Count);
                        Array.Copy(IntsToBytes(rgba), 0, cmap, cc * 7, 4);
                        Array.Copy(amountOfCubes, 0, cmap, cc * 7 + 4, 3);
                        foreach (Cube cube in sameColourCubes)
                        {
                            cubeBytes[cubeCounter * 6] = (byte)cube.Low.X;
                            cubeBytes[cubeCounter * 6 + 1] = (byte)cube.Low.Y;
                            cubeBytes[cubeCounter * 6 + 2] = (byte)cube.Low.Z;
                            cubeBytes[cubeCounter * 6 + 3] = (byte)cube.High.X;
                            cubeBytes[cubeCounter * 6 + 4] = (byte)cube.High.Y;
                            cubeBytes[cubeCounter * 6 + 5] = (byte)cube.High.Z;
                            cubeCounter++;
                        }
                    }
                    chunks.Add(new Chunk("CMAP", cmap));
                    chunks.Add(new Chunk("CUBE", cubeBytes));
                }
                if (voxels.Count > 0) // FIXME: refactor, duplicate code from cube writing
                {
                    List<IGrouping<Color, Cube>> sortedVoxels = voxels.GroupBy(c => c.Colour).ToList();
                    byte[] xyzBytes = new byte[voxels.Count * 3];
                    byte[] vmap = new byte[sortedVoxels.Count * 7];
                    int voxelCounter = 0;
                    for (int vv = 0; vv < sortedVoxels.Count; vv++)
                    {
                        List<Cube> sameColourVoxels = sortedVoxels[vv].ToList();
                        int rgba = sameColourVoxels.First().Colour.ToRgba();
                        byte[] amountOfVoxels = IntsToBytes(3, true, sameColourVoxels.Count);
                        Array.Copy(IntsToBytes(rgba), 0, vmap, vv * 7, 4);
                        Array.Copy(amountOfVoxels, 0, vmap, vv * 7 + 4, 3);
                        foreach (Cube voxel in sameColourVoxels)
                        {
                            XYZ xyz = voxel.Low;
                            xyzBytes[voxelCounter * 3] = (byte)xyz.X;
                            xyzBytes[voxelCounter * 3 + 1] = (byte)xyz.Y;
                            xyzBytes[voxelCounter * 3 + 2] = (byte)xyz.Z;
                            voxelCounter++;
                        }
                    }
                    chunks.Add(new Chunk("VMAP", vmap));
                    chunks.Add(new Chunk("XYZ ", xyzBytes));
                }
            }
            foreach (Chunk chunk in chunks)
                RiffWriter.WriteNextChunk(stream, chunk);
        }
    }
}
