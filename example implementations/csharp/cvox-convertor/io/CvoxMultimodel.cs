using cvox_convertor.voxel;

namespace cvox_convertor.io
{
    public class CvoxMultimodel
    {
        public List<CvoxModel> Models = new();
        public List<XYZ> Translations = new();
        public XYZ Size = new();
        static readonly XYZ ModelLimit = new(256);

        public CvoxMultimodel() { }

        public CvoxMultimodel(VoxModel voxModel)
        {
            Size = voxModel.Size;
            VoxModel[,,] voxModels = voxModel.SimpleSplit(ModelLimit);
            for (int xx = 0; xx < voxModels.GetLength(0); xx++)
                for (int yy = 0; yy < voxModels.GetLength(1); yy++)
                    for (int zz = 0; zz < voxModels.GetLength(2); zz++)
                        Add(new CvoxModel(voxModels[xx, yy, zz]), new XYZ(xx, yy, zz) * ModelLimit);
        }

        public void Add(CvoxModel model, XYZ translation)
        {
            Models.Add(model);
            Translations.Add(translation);
            Size = Size.Max(model.size + translation);
        }
    }
}
