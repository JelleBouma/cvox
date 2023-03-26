package io;

import rifflike.Chunk;
import rifflike.RiffWriter;
import utils.EL;
import utils.Grouping;
import utils.Groups;
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
            EL<Chunk> chunks = new EL<>(new Chunk(CvoxID.CVOX, intsToBytes(true,1)));
            for (int mm = 0; mm < cvoxMultimodel.models.size(); mm++) {
                CvoxModel model = cvoxMultimodel.models.get(mm);
                byte[] size = new byte[15];
                size[0] = (byte) model.size.x;
                size[1] = (byte) model.size.y;
                size[2] = (byte) model.size.z;
                XYZ translation = cvoxMultimodel.translations.get(mm);
                byte[] translationBytes = intsToBytes(true, translation.x, translation.y, translation.z);
                System.arraycopy(translationBytes, 0, size, 3, 12);
                chunks.add(new Chunk(CvoxID.SIZE, size));
                Groups<Colour, Cube> sortedCubes = model.filter(c -> !c.low.equals(c.high)).groupBy(c -> c.colour);
                Groups<Colour, Cube> sortedVoxels = model.filter(c -> c.low.equals(c.high)).groupBy(c -> c.colour);
                if (!sortedCubes.isEmpty()) {
                    ColourMap cmap = new ColourMap();
                    EL<XYZ> cubeXYZs = new EL<>();
                    for (Grouping<Colour, Cube> group : sortedCubes) {
                        cmap.add(group.key, group.size());
                        for (Cube cube : group) {
                            cubeXYZs.add(cube.low);
                            cubeXYZs.add(cube.high);
                        }
                    }
                    chunks.add(new Chunk(CvoxID.CMAP, cmap.toBytes()));
                    chunks.add(new Chunk(CvoxID.CUBE, xyzsToBytes(cubeXYZs)));
                }
                if (!sortedVoxels.isEmpty()) {
                    ColourMap vmap = new ColourMap();
                    EL<XYZ> voxelXYZs = new EL<>();
                    for (Grouping<Colour, Cube> group : sortedVoxels) {
                        vmap.add(group.key, group.size());
                        voxelXYZs.addAll(group.convertAll(c -> c.low));
                    }
                    chunks.add(new Chunk(CvoxID.VMAP, vmap.toBytes()));
                    chunks.add(new Chunk(CvoxID.XYZ, xyzsToBytes(voxelXYZs)));
                }
            }
            for (Chunk chunk : chunks)
                RiffWriter.writeNextChunk(dos, chunk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] xyzsToBytes(EL<XYZ> xyzs)
    {
        byte[] res = new byte[xyzs.size() * 3];
        for (int ii = 0; ii < xyzs.size(); ii++)
        {
            XYZ xyz = xyzs.get(ii);
            res[ii * 3] = (byte)xyz.x;
            res[ii * 3 + 1] = (byte)xyz.y;
            res[ii * 3 + 2] = (byte)xyz.z;
        }
        return res;
    }
}
