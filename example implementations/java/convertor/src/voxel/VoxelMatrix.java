package voxel;

public class VoxelMatrix {

    public XYZ size;
    public Palette palette;
    public int[][][] content;

    public VoxelMatrix(int[][][] content) {
        this.content = content;
        size = new XYZ(content.length, content[0].length, content[0][0].length);
    }

    public VoxelMatrix(int[][][] content, Palette palette) {
        this(content);
        this.palette = palette;
    }


    public void add(VoxelMatrix matrix) {
        add(new XYZ(), matrix);
    }

    public void add(XYZ offset, VoxelMatrix matrix) {
        new XYZ().fromTo(matrix.size, xyz -> {
            int i = matrix.content[xyz.x][xyz.y][xyz.z];
            if (i > 0) {
                xyz = xyz.add(offset);
                content[xyz.x][xyz.y][xyz.z] = i;
            }
        });
    }

    public VoxelMatrix clone() {
        int[][][] copy = new int[size.x][size.y][size.z];
        for (int xx = 0; xx < size.x; xx++)
            for (int yy = 0; yy < size.y; yy++)
                copy[xx][yy] = content[xx][yy].clone();
        return new VoxelMatrix(copy, palette.clone());
    }

}
