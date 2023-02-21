using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.rifflike
{
    public class Chunk
    {
        public string Id;
        public int Size;
        public byte[] Content;

        public Chunk(string id, byte[] bytes) => (Id, Size, Content) = (id, bytes.Length, bytes);
    }
}
