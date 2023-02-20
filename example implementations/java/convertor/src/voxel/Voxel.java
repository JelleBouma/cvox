package voxel;

public class Voxel extends XYZ {
    public int i;

    public Voxel(byte x, byte y, byte z, byte i) {
        this(x & 0xff, y & 0xff, z & 0xff, i & 0xff);
    }

    public Voxel(int x, int y, int z, int i) {
        super(x, y, z);
        this.i = i;
    }

    public Voxel(XYZ xyz, int i) {
        super(xyz.x, xyz.y, xyz.z);
        this.i = i;
    }
}
