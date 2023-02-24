using cvox_convertor.rifflike;
using cvox_convertor.utils;
using cvox_convertor.voxel;
using System.Drawing;
using System.Text;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.io
{
    public class VoxWriter
    {

        public static void Write(VoxModel voxelModel, Stream stream)
        {
            stream.Write(Encoding.ASCII.GetBytes("VOX "));
            stream.Write(IntsToBytes(true, 150));
            XYZ limit = new(256, 256, 256);
            bool multiModel = voxelModel.Size.GreaterThan(limit).Has(i => i > 0);
            VoxModel[,,] models = voxelModel.SimpleSplit(limit);
            int counter = 0;
            List<MagicaChunk> chunks = new();
            List<MagicaChunk> translations = new();
            for (int xx = 0; xx < models.GetLength(0); xx++)
                for (int yy = 0; yy < models.GetLength(1); yy++)
                    for (int zz = 0; zz < models.GetLength(2); zz++)
                    {
                        VoxModel model = models[xx, yy, zz];
                        chunks.Add(new MagicaChunk(VoxID.SIZE, IntsToBytes(true, model.Size.X, model.Size.Y, model.Size.Z)));
                        byte[] xyziBytes = new byte[4 + model.Count * 4];
                        Array.Copy(IntsToBytes(true, model.Count), xyziBytes, 4);
                        for (int vv = 0; vv < model.Count; vv++)
                        {
                            Voxel voxel = model[vv];
                            xyziBytes[4 + vv * 4] = (byte)voxel.X;
                            xyziBytes[5 + vv * 4] = (byte)voxel.Y;
                            xyziBytes[6 + vv * 4] = (byte)voxel.Z;
                            xyziBytes[7 + vv * 4] = (byte)voxel.I;
                        }
                        chunks.Add(new MagicaChunk(VoxID.XYZI, xyziBytes));
                        XYZ current = new(xx, yy, zz);
                        XYZ translationVector = (current * limit) + (model.Size / 2);
                        translations.AddRange(Translate(counter, translationVector, counter * 2 + 2));
                        counter++;
                    }
            List<int> rgbaPalette = new List<Color>(voxelModel.GetPalette().getArray()).ConvertAll(Extensions.ToRgba);
            rgbaPalette.Add(0);
            chunks.Add(new MagicaChunk(VoxID.RGBA, IntsToBytes(rgbaPalette)));
            if (multiModel)
            {
                chunks.Add(new MagicaChunk(VoxID.nTRN, IntsToBytes(true, 0, 0, 1, -1, -1, 1, 0)));
                byte[] nGRP = new byte[12 + counter * 4];
                Array.Copy(IntsToBytes(true, 1, 0, counter), nGRP, 12);
                for (int cc = 0; cc < counter; cc++)
                {
                    byte[] ccBytes = IntsToBytes(true, cc * 2 + 2);
                    Array.Copy(ccBytes, 0, nGRP, 12 + cc * 4, ccBytes.Length);
                }
                byte[] layr = new byte[26];
                Array.Copy(IntsToBytes(true, 0, 1, 5), layr, 12);
                Array.Copy(Encoding.ASCII.GetBytes("_name"), 0, layr, 12, 5);
                Array.Copy(IntsToBytes(true, 1), 0, layr, 17, 4);
                Array.Copy(Encoding.ASCII.GetBytes("0"), 0, layr, 21, 1);
                Array.Copy(IntsToBytes(true, -1), 0, layr, 22, 4);
                chunks.Add(new(VoxID.nGRP, nGRP));
                chunks.AddRange(translations);
                chunks.Add(new(VoxID.LAYR, layr));
            }
            MagicaChunk main = new(VoxID.MAIN, Array.Empty<byte>(), chunks);
            RiffWriter.WriteNextMagicaChunk(stream, main);
        }

        private static List<MagicaChunk> Translate(int modelID, XYZ translation, int nodeID)
        {
            List<MagicaChunk> res = new();
            string t = translation.X + " " + translation.Y + " " + translation.Z;
            byte[] nTRN = new byte[38 + t.Length];
            Array.Copy(IntsToBytes(true, nodeID, 0, nodeID + 1, -1, 0, 1, 1, 2), nTRN, 32);
            Array.Copy(Encoding.ASCII.GetBytes("_t"), 0, nTRN, 32, 2);
            Array.Copy(IntsToBytes(true, t.Length), 0, nTRN, 34, 4);
            Array.Copy(Encoding.ASCII.GetBytes(t), 0, nTRN, 38, t.Length);
            byte[] nSHP = IntsToBytes(true, nodeID + 1, 0, 1, modelID, 0);
            res.Add(new MagicaChunk(VoxID.nTRN, nTRN));
            res.Add(new MagicaChunk(VoxID.nSHP, nSHP));
            return res;
        }
    }
}
