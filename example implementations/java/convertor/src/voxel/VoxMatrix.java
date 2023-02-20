package voxel;

public class VoxMatrix {

    public XYZ size;
    public Palette palette;
    public int[][][] content;

    public VoxMatrix(int[][][] content) {
        this.content = content;
        size = new XYZ(content.length, content[0].length, content[0][0].length);
    }

    public VoxMatrix(int[][][] content, Palette palette) {
        this(content);
        this.palette = palette;
    }


    public void add(VoxMatrix matrix) {
        add(new XYZ(), matrix);
    }

    public void add(XYZ offset, VoxMatrix matrix) {
        new XYZ().fromTo(matrix.size, xyz -> {
            int i = matrix.content[xyz.x][xyz.y][xyz.z];
            if (i > 0) {
                xyz = xyz.add(offset);
                content[xyz.x][xyz.y][xyz.z] = i;
            }
        });
    }

    public int get(XYZ xyz) {
        return content[xyz.x][xyz.y][xyz.z];
    }

}
