using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using cvox_convertor.utils;

namespace cvox_convertor.rifflike
{
    public class MagicaChunk
    {
        public string Id;
        public int Size;
        public int SubchunkSize;

        public byte[] Content;
        public List<MagicaChunk> SubChunks;

        public MagicaChunk(string id, byte[] bytes) : this(id, bytes, new()) { }

        public MagicaChunk(string id, byte[] bytes, List<MagicaChunk> subChunks)
        {
            Id = id;
            Size = bytes.Length;
            Content = bytes;
            SubChunks = subChunks;
            SubchunkSize = subChunks.ConvertAll(c => c.Size + 12).Sum();
        }

        public List<MagicaChunk> GetSubChunks(string id)
        {
            return SubChunks.AllMatches(c => c.Id == id);
        }

        public MagicaChunk getSubChunk(string id)
        {
            return SubChunks.FirstMatch(c => c.Id == id);
        }
    }
}
