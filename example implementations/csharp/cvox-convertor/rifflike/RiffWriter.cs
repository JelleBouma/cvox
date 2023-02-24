using System.Text;
using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.rifflike
{
    public class RiffWriter
    {
        public static void WriteNextMagicaChunk(Stream stream, MagicaChunk chunk)
        {
            stream.Write(Encoding.ASCII.GetBytes(chunk.Id));
            stream.Write(IntsToBytes(true, chunk.Size));
            stream.Write(IntsToBytes(true, chunk.SubchunkSize));
            stream.Write(chunk.Content);
            foreach (MagicaChunk subchunk in chunk.SubChunks)
                WriteNextMagicaChunk(stream, subchunk);
        }
        public static void WriteNextChunk(Stream stream, Chunk chunk)
        {
            stream.Write(Encoding.ASCII.GetBytes(chunk.Id));
            stream.Write(IntsToBytes(true, chunk.Size));
            stream.Write(chunk.Content);
        }
    }
}
