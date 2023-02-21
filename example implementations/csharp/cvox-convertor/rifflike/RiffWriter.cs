using cvox_convertor.rifflike;
using static cvox_convertor.utils.NumberUtilities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.rifflike
{
    public class RiffWriter
    {
        public static void writeNextMagicaChunk(FileStream stream, MagicaChunk chunk)
        {
            Console.WriteLine("wrting " + chunk.Id);
            stream.Write(Encoding.ASCII.GetBytes(chunk.Id));
            stream.Write(IntsToBytes(true, chunk.Size));
            stream.Write(IntsToBytes(true, chunk.SubchunkSize));
            stream.Write(chunk.Content);
            foreach (MagicaChunk subchunk in chunk.SubChunks)
                writeNextMagicaChunk(stream, subchunk);
        }
        public static void writeNextChunk(FileStream stream, Chunk chunk)
        {
            stream.Write(Encoding.ASCII.GetBytes(chunk.Id));
            stream.Write(IntsToBytes(true, chunk.Size));
            stream.Write(chunk.Content);
        }
    }
}
