namespace cvox_convertor.voxel
{
    public class VoxMatrix
    {

        public XYZ size;
        public Palette palette;
        public int[,,] Content;

        public VoxMatrix(int[,,] content)
        {
            Content = content;
            size = new XYZ(content.GetLength(0), content.GetLength(1), content.GetLength(2));
        }

        public VoxMatrix(int[,,] content, Palette palette) : this(content)
        {
            this.palette = palette;
        }

        public void Add(XYZ offset, VoxMatrix matrix)
        {
            new XYZ().FromTo(matrix.size, xyz =>
            {
                int i = matrix.Content[xyz.X, xyz.Y, xyz.Z];
                if (i > 0)
                {
                    xyz += offset;
                    Content[xyz.X, xyz.Y, xyz.Z] = i;
                }
            });
        }

        public int Get(XYZ xyz)
        {
            return Content[xyz.X, xyz.Y, xyz.Z];
        }

    }
}
