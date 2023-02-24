using cvox_convertor.io;
using System.Text;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.rifflike
{
    public class RiffReader
    {

        public static async Task<MagicaChunk?> ReadNextMagicaChunkAsync(Stream stream)
        {
            byte[] meta = new byte[12];
            int success = await stream.ReadAsync(meta.AsMemory(0, 12));
            if (success == 12)
            {
                string chunkID = Encoding.ASCII.GetString(meta[0..4]);
                int size = BytesToInt(meta[4..8], true);
                int subChunkSize = BytesToInt(meta[8..12], true);
                byte[] content = new byte[size];
                success = await stream.ReadAsync(content.AsMemory(0, size));
                if (success == size)
                {
                    List<MagicaChunk> subchunks = new();
                    int chunkCounter = 0;
                    while (chunkCounter < subChunkSize)
                    {
                        MagicaChunk subchunk = await ReadNextMagicaChunkAsync(stream);
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
        public async static Task<Chunk?> ReadNextChunkAsync(Stream stream)
        {
            byte[] meta = new byte[8];
            int success = await stream.ReadAsync(meta.AsMemory(0, 8));
            if (success == 8)
            {
                string chunkID = Encoding.ASCII.GetString(meta[0..4]);
                int size = BytesToInt(meta[4..8], true);
                byte[] content = new byte[size];
                success = await stream.ReadAsync(content.AsMemory(0, size));
                if (success == size)
                    return new Chunk(chunkID, content);
                throw new InvalidCvoxException(".cvox input has an invalid chunk structure");
            }
            return null;
        }
    }
}
