package rifflike;

public class Chunk {
    public String id;
    public int size;
    public byte[] content;

    public Chunk(String id, byte[] bytes) {
        this.id = id;
        this.size = bytes.length;
        this.content = bytes;
    }
}