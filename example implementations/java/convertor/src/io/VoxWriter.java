package io;

import rifflike.MagicaChunk;
import rifflike.RiffWriter;
import utils.EL;
import utils.NumberUtilities;
import voxel.Colour;
import voxel.VoxModel;
import voxel.Voxel;
import voxel.XYZ;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class VoxWriter {

    public static void write(VoxModel voxelModel, File output) {
        long startTime = System.currentTimeMillis();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(output))) {
            dos.writeBytes("VOX ");
            dos.write(NumberUtilities.intsToBytes(true, 150));
            XYZ limit = new XYZ(256,256,256);
            boolean multiModel = voxelModel.size.greaterThan(limit).has(i -> i > 0);
            VoxModel[][][] models = voxelModel.simpleSplit(limit);
            int counter = 0;
            EL<MagicaChunk> chunks = new EL<>();
            EL<MagicaChunk> translations = new EL<>();
            for (int xx = 0; xx < models.length; xx++)
                for (int yy = 0; yy < models[xx].length; yy++)
                    for (int zz = 0; zz < models[xx][yy].length; zz++) {
                        VoxModel model = models[xx][yy][zz];
                        chunks.add(new MagicaChunk("SIZE", NumberUtilities.intsToBytes(true, model.size.x, model.size.y, model.size.z)));
                        byte[] xyziBytes = Arrays.copyOf(NumberUtilities.intsToBytes(true, model.size()), 4 + model.size() * 4);
                        for (int vv = 0; vv < model.size(); vv++) {
                            Voxel voxel = model.get(vv);
                            xyziBytes[4 + vv * 4] = (byte) voxel.x;
                            xyziBytes[5 + vv * 4] = (byte) voxel.y;
                            xyziBytes[6 + vv * 4] = (byte) voxel.z;
                            xyziBytes[7 + vv * 4] = (byte) voxel.i;
                        }
                        chunks.add(new MagicaChunk("XYZI", xyziBytes));
                        XYZ current = new XYZ(xx, yy, zz);
                        XYZ translationVector = current.mult(limit).add(model.size.div(2));
                        translations.addAll(translate(counter, translationVector, counter * 2 + 2));
                        counter++;
                    }
            EL<Integer> rgbaPalette = new EL<>(voxelModel.getPalette().getArray()).convertAll(c -> Colour.argbToRgba(c.getRGB()));
            rgbaPalette.add(0);
            chunks.add(new MagicaChunk("RGBA", NumberUtilities.intsToBytes(rgbaPalette)));
            if (multiModel) {
                chunks.add(new MagicaChunk("nTRN", NumberUtilities.intsToBytes(true, 0, 0, 1, -1, -1, 1, 0)));
                byte[] nGRP = Arrays.copyOf(NumberUtilities.intsToBytes(true, 1, 0, counter), 12 + counter * 4);
                for (int cc = 0; cc < counter; cc++) {
                    byte[] ccBytes = NumberUtilities.intsToBytes(true, cc * 2 + 2);
                    System.arraycopy(ccBytes, 0, nGRP, 12 + cc * 4, ccBytes.length);
                }
                byte[] layr = Arrays.copyOf(NumberUtilities.intsToBytes(true, 0, 1, 5), 26);
                System.arraycopy("_name".getBytes(), 0, layr, 12, 5);
                System.arraycopy(NumberUtilities.intsToBytes(true, 1), 0, layr, 17, 4);
                System.arraycopy("0".getBytes(), 0, layr, 21, 1);
                System.arraycopy(NumberUtilities.intsToBytes(true, -1), 0, layr, 22, 4);
                chunks.add(new MagicaChunk("nGRP", nGRP));
                chunks.addAll(translations);
                chunks.add(new MagicaChunk("LAYR", layr));
            }
            MagicaChunk main = new MagicaChunk("MAIN", new byte[0], chunks);
            RiffWriter.writeNextMagicaChunk(dos, main);
            System.out.println("writing voxelmodel " + output.getName() + " took " + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static EL<MagicaChunk> translate(int modelID, XYZ translation, int nodeID) {
        String t = translation.x + " " + translation.y + " " + translation.z;
        byte[] nTRN = Arrays.copyOf(NumberUtilities.intsToBytes(true, nodeID, 0, nodeID + 1, -1, 0, 1, 1, 2), 38 + t.length());
        System.arraycopy("_t".getBytes(), 0, nTRN, 32, 2);
        System.arraycopy(NumberUtilities.intsToBytes(true, t.length()), 0, nTRN, 34, 4);
        System.arraycopy(t.getBytes(), 0, nTRN, 38, t.length());
        byte[] nSHP = NumberUtilities.intsToBytes(true, nodeID + 1, 0, 1, modelID, 0);
        return new EL<>(new MagicaChunk("nTRN", nTRN), new MagicaChunk("nSHP", nSHP));
    }
}
