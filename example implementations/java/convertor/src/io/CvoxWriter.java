package io;

import rifflike.Chunk;
import rifflike.RiffWriter;
import utils.EL;
import voxel.Colour;
import voxel.Cube;
import voxel.XYZ;

import static utils.NumberUtilities.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CvoxWriter {

    public static void write(CvoxMultimodel cvoxMultimodel, File output) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(output))) {
            EL<Chunk> chunks = new EL<>(new Chunk("CVOX", intsToBytes(true,1)));
            for (int mm = 0; mm < cvoxMultimodel.models.size(); mm++) {
                CvoxModel model = cvoxMultimodel.models.get(mm);
                byte[] size = new byte[15];
                size[0] = (byte) model.size.x;
                size[1] = (byte) model.size.y;
                size[2] = (byte) model.size.z;
                XYZ translation = cvoxMultimodel.translations.get(mm);
                byte[] translationBytes = intsToBytes(true, translation.x, translation.y, translation.z);
                System.arraycopy(translationBytes, 0, size, 3, 12);
                chunks.add(new Chunk("SIZE", size));
                EL<Cube> voxels = model.filter(c -> c.low.equals(c.high));
                EL<Cube> cubes = model.filter(c -> !c.low.equals(c.high));
                if (cubes.size() > 0) {
                    EL<EL<Cube>> sortedCubes = cubes.distribute((c1, c2) -> c1.colour.equals(c2.colour));
                    byte[] cubeBytes = new byte[cubes.size() * 6];
                    byte[] cmap = new byte[sortedCubes.size() * 7];
                    int cubeCounter = 0;
                    for (int cc = 0; cc < sortedCubes.size(); cc++) {
                        EL<Cube> sameColourCubes = sortedCubes.get(cc);
                        int rgba = Colour.argbToRgba(sameColourCubes.first().colour.getRGB());
                        byte[] amountOfCubes = intsToBytes(3, true, sameColourCubes.size());
                        System.arraycopy(intsToBytes(rgba), 0, cmap, cc * 7, 4);
                        System.arraycopy(amountOfCubes, 0, cmap, cc * 7 + 4, 3);
                        for (Cube cube : sameColourCubes) {
                            cubeBytes[cubeCounter * 6] = (byte) cube.low.x;
                            cubeBytes[cubeCounter * 6 + 1] = (byte) cube.low.y;
                            cubeBytes[cubeCounter * 6 + 2] = (byte) cube.low.z;
                            cubeBytes[cubeCounter * 6 + 3] = (byte) cube.high.x;
                            cubeBytes[cubeCounter * 6 + 4] = (byte) cube.high.y;
                            cubeBytes[cubeCounter * 6 + 5] = (byte) cube.high.z;
                            cubeCounter++;
                        }
                    }
                    chunks.add(new Chunk("CMAP", cmap));
                    chunks.add(new Chunk("CUBE", cubeBytes));
                }
                if (voxels.size() > 0) { // FIXME: refactor, duplicate code from cube writing
                    EL<EL<Cube>> sortedVoxels = voxels.distribute((c1, c2) -> c1.colour.equals(c2.colour));
                    byte[] xyzBytes = new byte[voxels.size() * 3];
                    byte[] vmap = new byte[sortedVoxels.size() * 7];
                    int voxelCounter = 0;
                    for (int vv = 0; vv < sortedVoxels.size(); vv++) {
                        EL<Cube> sameColourVoxels = sortedVoxels.get(vv);
                        int rgba = Colour.argbToRgba(sameColourVoxels.first().colour.getRGB());
                        byte[] amountOfVoxels = intsToBytes(3, true, sameColourVoxels.size());
                        System.arraycopy(intsToBytes(rgba), 0, vmap, vv * 7, 4);
                        System.arraycopy(amountOfVoxels, 0, vmap, vv * 7 + 4, 3);
                        for (Cube voxel : sameColourVoxels) {
                            XYZ xyz = voxel.low;
                            xyzBytes[voxelCounter * 3] = (byte) xyz.x;
                            xyzBytes[voxelCounter * 3 + 1] = (byte) xyz.y;
                            xyzBytes[ voxelCounter * 3 + 2] = (byte) xyz.z;
                            voxelCounter++;
                        }
                    }
                    chunks.add(new Chunk("VMAP", vmap));
                    chunks.add(new Chunk("XYZ ", xyzBytes));
                }
            }
            for (Chunk chunk : chunks)
                RiffWriter.writeNextChunk(dos, chunk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
