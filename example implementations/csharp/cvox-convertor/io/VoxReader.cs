﻿using cvox_convertor.rifflike;
using cvox_convertor.voxel;
using System.Text;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.io
{
    public class InvalidVoxException : Exception
    {
        public InvalidVoxException(string msg) : base(msg) { }
    }

    public class VoxReader
    {
        public static async Task<VoxModel> ReadAsync(Stream stream)
        {
            await stream.ReadAsync(new byte[8]);
            MagicaChunk? main = await RiffReader.ReadNextMagicaChunkAsync(stream);
            if (main == null)
                throw new InvalidVoxException("could not find MAIN chunk");
            List<MagicaChunk> sizeChunks = main.GetSubChunks(VoxID.SIZE);
            List<MagicaChunk> xyziChunks = main.GetSubChunks(VoxID.XYZI);
            byte[] rgba = main.GetSubChunk(VoxID.RGBA).Content;
            List<VoxModel> models = new();
            for (int ss = 0; ss < sizeChunks.Count; ss++)
            {
                byte[] sizeBytes = sizeChunks[ss].Content;
                int[] size = new int[3];
                for (int ii = 0; ii < 3; ii++)
                    size[ii] = BytesToInt(sizeBytes[(ii * 4)..((ii + 1) * 4)], true);
                VoxModel model = new VoxModel(size[0], size[1], size[2]);
                model.FillXYZI(xyziChunks[ss].Content);
                model.SetPalette(new Palette(rgba));
                models.Add(model);
            }
            Dictionary<int, XYZ> translations = ReadTranslations(main.GetSubChunks(VoxID.nTRN), main.GetSubChunks(VoxID.nSHP));
            return VoxModel.SimpleMerge(models, translations, models[0].GetPalette());
        }

        /**
         * Read the .vox model translations
         */
        private static Dictionary<int, XYZ> ReadTranslations(List<MagicaChunk> nTRNs, List<MagicaChunk> nSHPs)
        {
            Dictionary<int, XYZ> res = new();
            foreach (MagicaChunk nTRN in nTRNs)
            {
                byte[] bytes = nTRN.Content;
                int amountOfAttributes = BytesToInt(bytes[4..8], true);
                int pointer = 8;
                for (int aa = 0; aa < amountOfAttributes * 2; aa++)
                {
                    int stringSize = BytesToInt(bytes[pointer..(pointer + 4)], true);
                    pointer += 4 + stringSize;
                }
                int childID = BytesToInt(bytes[pointer..(pointer + 4)], true);
                pointer += 12;
                int frames = BytesToInt(bytes[pointer..(pointer + 4)], true);
                pointer += 4;
                for (int ff = 0; ff < frames; ff += 2)
                {
                    int dictSize = BytesToInt(bytes[pointer..(pointer + 4)], true);
                    pointer += 4;
                    for (int dd = 0; dd < dictSize; dd++)
                    {
                        int keySize = BytesToInt(bytes[pointer..(pointer + 4)], true);
                        pointer += 4;
                        string key = Encoding.ASCII.GetString(bytes[pointer..(pointer + keySize)]);
                        pointer += keySize;
                        int valueSize = BytesToInt(bytes[pointer..(pointer + 4)], true);
                        pointer += 4;
                        string value = Encoding.ASCII.GetString(bytes[pointer..(pointer + valueSize)]);
                        pointer += valueSize;
                        if (key == "_t")
                        {
                            string[] xyzParts = value.Split(' ');
                            XYZ xyz = new(int.Parse(xyzParts[0]), int.Parse(xyzParts[1]), int.Parse(xyzParts[2]));
                            foreach (MagicaChunk nSHP in nSHPs)
                            {
                                byte[] nshpBytes = nSHP.Content;
                                int nshpID = BytesToInt(nshpBytes[0..4], true);
                                if (nshpID == childID)
                                {
                                    int nshpAttributes = BytesToInt(nshpBytes[4..8], true);
                                    int nshpPointer = 8;
                                    for (int aa = 0; aa < nshpAttributes * 2; aa++)
                                    {
                                        int stringSize = BytesToInt(nshpBytes[nshpPointer..(nshpPointer + 4)], true);
                                        nshpPointer += 4 + stringSize;
                                    }
                                    int models = BytesToInt(nshpBytes[nshpPointer..(nshpPointer + 4)], true);
                                    nshpPointer += 4;
                                    for (int mm = 0; mm < models; mm++)
                                    {
                                        int modelID = BytesToInt(nshpBytes[nshpPointer..(nshpPointer + 4)], true);
                                        nshpPointer += 4;
                                        res.Add(modelID, xyz);
                                        int modelAttributes = BytesToInt(nshpBytes[nshpPointer..(nshpPointer + 4)], true);
                                        nshpPointer += 4;
                                        for (int aa = 0; aa < modelAttributes * 2; aa++)
                                        {
                                            int stringSize = BytesToInt(nshpBytes[nshpPointer..(nshpPointer + 4)], true);
                                            nshpPointer += 4 + stringSize;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return res;
        }
    }
}
