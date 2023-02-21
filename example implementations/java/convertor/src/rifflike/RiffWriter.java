package rifflike;

import static utils.NumberUtilities.intsToBytes;

import java.io.DataOutputStream;
import java.io.IOException;

public class RiffWriter {
    public static void writeNextMagicaChunk(DataOutputStream dos, MagicaChunk chunk) throws IOException {
        dos.writeBytes(chunk.id);
        dos.write(intsToBytes(true, chunk.size));
        dos.write(intsToBytes(true, chunk.subchunkSize));
        dos.write(chunk.content);
        for (MagicaChunk subchunk : chunk.subChunks)
            writeNextMagicaChunk(dos, subchunk);
    }

    public static void writeNextChunk(DataOutputStream dos, Chunk chunk) throws IOException {
        dos.writeBytes(chunk.id);
        dos.write(intsToBytes(true, chunk.size));
        dos.write(chunk.content);
    }
}
