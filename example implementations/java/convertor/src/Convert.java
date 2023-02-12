import io.*;
import voxel.VoxModel;

import java.io.File;

public class Convert {
    public static void main(String[] args) {
        File input = new File(args[0]);
        File output = new File(args[1]);
        String inputType = args[0].substring(args[0].lastIndexOf("."));
        if (inputType.equals(".vox"))
            CvoxWriter.write(new CvoxMultimodel(VoxReader.read(input)), output);
        else
            VoxWriter.write(new VoxModel(CvoxReader.read(input)), output);
    }
}
