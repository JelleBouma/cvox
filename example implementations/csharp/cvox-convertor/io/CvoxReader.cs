using cvox_convertor.rifflike;
using cvox_convertor.voxel;
using System.Drawing;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.io
{
    public class InvalidCvoxException : Exception
    {
        public InvalidCvoxException(string msg) : base(msg) { }
    }

    public class CvoxReader
    {
        public static async Task<CvoxMultimodel> ReadAsync(Stream stream)
        {
            CvoxMultimodel res = new();
            Chunk? chunk = await RiffReader.ReadNextChunkAsync(stream);
            if (chunk == null || chunk.Id != CvoxID.CVOX)
                throw new InvalidCvoxException("Not a valid cvox file, it should start with the CVOX chunk");
            CvoxModel? model = null;
            ColourMap cmap = new();
            ColourMap vmap = new();
            List<Cube> cubes = new();
            List<Cube> voxels = new();
            chunk = await RiffReader.ReadNextChunkAsync(stream);
            while (chunk != null)
            {
                switch (chunk.Id)
                {
                    case CvoxID.SIZE:
                        if (model != null)
                        {
                            model.Fill(cmap, cubes);
                            model.Fill(vmap, voxels);
                        }
                        model = new CvoxModel(ReadDimensionsFromSIZE(chunk));
                        res.Add(model, ReadTranslationFromSIZE(chunk));
                        break;
                    case CvoxID.CMAP:
                        cmap = ReadMap(chunk);
                        break;
                    case CvoxID.VMAP:
                        vmap = ReadMap(chunk);
                        break;
                    case CvoxID.XYZ:
                        voxels = ReadVoxels(chunk);
                        break;
                    case CvoxID.CUBE:
                        cubes = ReadCubes(chunk);
                        break;
                }
                chunk = await RiffReader.ReadNextChunkAsync(stream);
            }
            if (model != null)
            {
                model.Fill(cmap, cubes);
                model.Fill(vmap, voxels);
            }
            return res;
        }

        private static XYZ ReadDimensionsFromSIZE(Chunk chunk)
        {
            return ReadXYZFromBytes(chunk.Content);
        }

        private static XYZ ReadTranslationFromSIZE(Chunk chunk)
        {
            byte[] translationX, translationY, translationZ;
            translationX = translationY = translationZ = new byte[4];
            Array.Copy(chunk.Content, 3, translationX, 0, 4);
            Array.Copy(chunk.Content, 7, translationY, 0, 4);
            Array.Copy(chunk.Content, 11, translationZ, 0, 4);
            return new XYZ(BytesToInt(translationX, true), BytesToInt(translationY, true), BytesToInt(translationZ, true));
        }

        private static ColourMap ReadMap(Chunk chunk)
        {
            if (chunk.Size % 7 != 0)
                throw new InvalidCvoxException(chunk.Id + " is invalid as it has a byte count not divisible by 7");
            ColourMap res = new();
            int bb = 0;
            byte[] colour = new byte[4];
            byte[] count = new byte[3];
            while (bb < chunk.Content.Length)
            {
                Array.Copy(chunk.Content, bb, colour, 0, 4);
                Array.Copy(chunk.Content, bb + 4, count, 0, 3);
                res.Add(Color.FromArgb(colour[3], colour[0], colour[1], colour[2]), BytesToInt(count, true, 3));
                bb += 7;
            }
            return res;
        }

        private static List<Cube> ReadVoxels(Chunk chunk)
        {
            List<Cube> res = new();
            for (int bb = 0; bb < chunk.Size; bb += 3)
            {
                byte[] lowBytes = new byte[3];
                Array.Copy(chunk.Content, bb, lowBytes, 0, 3);
                XYZ low = ReadXYZFromBytes(lowBytes);
                res.Add(new Cube(low, low));
            }
            return res;
        }

        private static List<Cube> ReadCubes(Chunk chunk)
        {
            List<Cube> res = new();
            for (int bb = 0; bb < chunk.Size; bb += 6)
            {
                byte[] lowBytes = new byte[3];
                byte[] highBytes = new byte[3];
                Array.Copy(chunk.Content, bb, lowBytes, 0, 3);
                Array.Copy(chunk.Content, bb + 3, highBytes, 0, 3);
                XYZ low = ReadXYZFromBytes(lowBytes);
                XYZ high = ReadXYZFromBytes(highBytes);
                res.Add(new Cube(low, high));
            }
            return res;
        }

        private static XYZ ReadXYZFromBytes(byte[] bytes)
        {
            int zero = 0; // used to read unsigned value from a signed byte
            return new XYZ(zero | bytes[0], zero | bytes[1], zero | bytes[2]);
        }
    }
}