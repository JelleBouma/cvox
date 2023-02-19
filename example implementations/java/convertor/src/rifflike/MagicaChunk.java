package rifflike;

import utils.EL;
import static utils.NumberUtilities.sum;

public class MagicaChunk {
    public String id;
    public int size;
    public int subchunkSize;

    public byte[] content;
    public EL<MagicaChunk> subChunks;


    public MagicaChunk(String id, byte[] bytes) {
        this(id, bytes, new EL<>());
    }
    public MagicaChunk(String id, byte[] bytes, EL<MagicaChunk> subChunks) {
        this.id = id;
        this.size = bytes.length;
        this.content = bytes;
        this.subChunks = subChunks;
        subchunkSize = sum(subChunks.convertAll(c -> c.size + 12));
    }

    public EL<MagicaChunk> getSubChunks(String id) {
        return subChunks.allMatches(c -> c.id.equals(id));
    }

    public MagicaChunk getSubChunk(String id) {
        return subChunks.firstMatch(c -> c.id.equals(id));
    }
}
