package io;

import rifflike.MagicaChunk;
import rifflike.RiffReader;
import utils.EL;
import static utils.NumberUtilities.bytesToInt;
import voxel.Palette;
import voxel.VoxModel;
import voxel.XYZ;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class VoxReader {

    public static VoxModel read(File file) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            dis.skipBytes(8);
            MagicaChunk main = RiffReader.readNextMagicaChunk(dis);
            EL<MagicaChunk> sizeChunks = main.getSubChunks(VoxID.SIZE);
            EL<MagicaChunk> xyziChunks = main.getSubChunks(VoxID.XYZI);
            byte[] rgba = main.getSubChunk(VoxID.RGBA).content;
            EL<VoxModel> models = new EL<>();
            for (int ss = 0; ss < sizeChunks.size(); ss++) {
                byte[] sizeBytes = sizeChunks.get(ss).content;
                int[] size = new int[3];
                for (int ii = 0; ii < 3; ii++)
                    size[ii] = bytesToInt(Arrays.copyOfRange(sizeBytes, ii * 4, (ii + 1) * 4), true);
                VoxModel model = new VoxModel(size[0], size[1], size[2]);
                model.fillXYZI(xyziChunks.get(ss).content);
                model.setPalette(new Palette(rgba));
                models.add(model);
            }
            HashMap<Integer, XYZ> translations = readTranslations(main.getSubChunks(VoxID.nTRN), main.getSubChunks(VoxID.nSHP));
            return VoxModel.simpleMerge(models, translations, models.get(0).getPalette());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the .vox model translations
     */
    private static HashMap<Integer, XYZ> readTranslations(EL<MagicaChunk> nTRNs, EL<MagicaChunk> nSHPs) {
        HashMap<Integer, XYZ> res = new HashMap<>();
        for (MagicaChunk nTRN : nTRNs) {
            byte[] bytes = nTRN.content;
            int amountOfAttributes = bytesToInt(Arrays.copyOfRange(bytes, 4, 8), true);
            int pointer = 8;
            for (int aa = 0; aa < amountOfAttributes * 2; aa++) {
                int stringSize = bytesToInt(Arrays.copyOfRange(bytes, pointer, pointer + 4), true);
                pointer += 4 + stringSize;
            }
            int childID = bytesToInt(Arrays.copyOfRange(bytes, pointer, pointer + 4), true);
            pointer += 12;
            int frames = bytesToInt(Arrays.copyOfRange(bytes, pointer, pointer + 4), true);
            pointer += 4;
            for (int ff = 0; ff < frames; ff += 2) {
                int dictSize = bytesToInt(Arrays.copyOfRange(bytes, pointer, pointer + 4), true);
                pointer += 4;
                for (int dd = 0; dd < dictSize; dd++) {
                    int keySize = bytesToInt(Arrays.copyOfRange(bytes, pointer, pointer + 4), true);
                    pointer += 4;
                    String key = new String(Arrays.copyOfRange(bytes, pointer, pointer + keySize));
                    pointer += keySize;
                    int valueSize = bytesToInt(Arrays.copyOfRange(bytes, pointer, pointer + 4), true);
                    pointer += 4;
                    String value = new String(Arrays.copyOfRange(bytes, pointer, pointer + valueSize));
                    pointer += valueSize;
                    if (key.equals("_t")) {
                        String[] xyzParts = value.split(" ");
                        XYZ xyz = new XYZ(Integer.parseInt(xyzParts[0]), Integer.parseInt(xyzParts[1]), Integer.parseInt(xyzParts[2]));
                        for (MagicaChunk nSHP : nSHPs) {
                            byte[] nshpBytes = nSHP.content;
                            int nshpID = bytesToInt(Arrays.copyOfRange(nshpBytes, 0, 4), true);
                            if (nshpID == childID) {
                                int nshpAttributes = bytesToInt(Arrays.copyOfRange(nshpBytes, 4, 8), true);
                                int nshpPointer = 8;
                                for (int aa = 0; aa < nshpAttributes * 2; aa++) {
                                    int stringSize = bytesToInt(Arrays.copyOfRange(nshpBytes, nshpPointer, nshpPointer + 4), true);
                                    nshpPointer += 4 + stringSize;
                                }
                                int models = bytesToInt(Arrays.copyOfRange(nshpBytes, nshpPointer, nshpPointer + 4), true);
                                nshpPointer += 4;
                                for (int mm = 0; mm < models; mm++) {
                                    int modelID = bytesToInt(Arrays.copyOfRange(nshpBytes, nshpPointer, nshpPointer + 4), true);
                                    nshpPointer += 4;
                                    res.put(modelID, xyz);
                                    int modelAttributes = bytesToInt(Arrays.copyOfRange(nshpBytes, nshpPointer, nshpPointer + 4), true);
                                    nshpPointer += 4;
                                    for (int aa = 0; aa < modelAttributes * 2; aa++) {
                                        int stringSize = bytesToInt(Arrays.copyOfRange(nshpBytes, nshpPointer, nshpPointer + 4), true);
                                        nshpPointer += 4 + stringSize;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
}
