package rifflike;

import utils.EL;
import static utils.NumberUtilities.bytesToInt;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RiffReader {

    public static MagicaChunk readNextMagicaChunk(DataInputStream dis) throws IOException {
        String chunkID = new String(dis.readNBytes(4), StandardCharsets.US_ASCII);
        int size = bytesToInt(dis.readNBytes(4), true);
        int subChunkSize = bytesToInt(dis.readNBytes(4), true);
        byte[] content = dis.readNBytes(size);
        EL<MagicaChunk> subchunks = new EL<>();
        int chunkCounter = 0;
        while (Integer.compareUnsigned(chunkCounter, subChunkSize) < 0) {
            MagicaChunk subchunk = readNextMagicaChunk(dis);
            chunkCounter += subchunk.size + 12;
            subchunks.add(subchunk);
        }
        return new MagicaChunk(chunkID, content, subchunks);
    }

    /**
     * Reads a rifflike.Chunk (RIFF) from a data stream.
     * @return The SimpleChunk read, or null if the end of the stream has been reached.
     */
    public static Chunk readNextChunk(DataInputStream dis) throws IOException {
        String chunkID = new String(dis.readNBytes(4), StandardCharsets.US_ASCII);
        if (chunkID.isEmpty())
            return null;
        int size = bytesToInt(dis.readNBytes(4), true);
        byte[] content = dis.readNBytes(size);
        return new Chunk(chunkID, content);
    }
}
