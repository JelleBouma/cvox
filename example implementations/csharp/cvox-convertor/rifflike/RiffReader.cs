using cvox_convertor.io;
using cvox_convertor.rifflike;
using cvox_convertor.utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.rifflike
{
    public class RiffReader
    {

        public static MagicaChunk? readNextMagicaChunk(FileStream stream)
        {
            byte[] meta = new byte[12];
            int success = stream.Read(meta, 0, 12);
            if (success == 12)
            {
                string chunkID = Encoding.ASCII.GetString(meta[0..4]);
                int size = BytesToInt(meta[4..8], true);
                int subChunkSize = BytesToInt(meta[8..12], true);
                byte[] content = new byte[size];
                success = stream.Read(content, 0, size);
                if (success == size)
                {
                    List<MagicaChunk> subchunks = new();
                    int chunkCounter = 0;
                    while (chunkCounter < subChunkSize)
                    {
                        MagicaChunk subchunk = readNextMagicaChunk(stream);
                        chunkCounter += subchunk.Size + 12;
                        subchunks.Add(subchunk);
                    }
                    return new MagicaChunk(chunkID, content, subchunks);
                }
                throw new InvalidVoxException(".vox input has an invalid Magica chunk structure");
            }
            return null;
        }

        /**
         * Reads a rifflike.Chunk (RIFF) from a data stream.
         * @return The Chunk read, or null if the end of the stream has been reached.
         */
        public static Chunk? ReadNextChunk(FileStream stream)
        {
            byte[] meta = new byte[8];
            int success = stream.Read(meta, 0, 8);
            if (success == 8)
            {
                string chunkID = Encoding.ASCII.GetString(meta[0..4]);
                int size = BytesToInt(meta[4..8], true);
                byte[] content = new byte[size];
                success = stream.Read(content, 0, size);
                if (success == size)
                    return new Chunk(chunkID, content);
                throw new InvalidCvoxException(".cvox input has an invalid chunk structure");
            }
            return null;
        }
    }
}
