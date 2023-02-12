package io;

import utils.EL;
import voxel.VoxModel;
import voxel.XYZ;

public class CvoxMultimodel {
    public EL<CVoxModel> models = new EL<>();
    public EL<XYZ> translations = new EL<>();
    public XYZ size = new XYZ();
    static XYZ modelLimit = new XYZ(256);

    public CvoxMultimodel(){}

    public CvoxMultimodel(VoxModel voxModel) {
        size = voxModel.size;
        VoxModel[][][] voxModels = voxModel.simpleSplit(modelLimit);
        for (int xx = 0; xx < voxModels.length; xx++)
            for (int yy = 0; yy < voxModels[xx].length; yy++)
                for (int zz = 0; zz < voxModels[xx][yy].length; zz++)
                    add(new CVoxModel(voxModels[xx][yy][zz]), new XYZ(xx, yy, zz).mult(modelLimit));
    }

    public void add(CVoxModel model, XYZ translation) {
        models.add(model);
        translations.add(translation);
        size = size.max(model.size.add(translation));
    }
}
