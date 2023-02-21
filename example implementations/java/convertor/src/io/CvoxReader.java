package io;

import rifflike.Chunk;
import rifflike.RiffReader;
import utils.EL;
import voxel.Colour;
import voxel.Cube;
import voxel.XYZ;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static utils.NumberUtilities.bytesToInt;

public class CvoxReader {
    public static CvoxMultimodel read(File file) {
        CvoxMultimodel res = new CvoxMultimodel();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            Chunk chunk = RiffReader.readNextChunk(dis);
            if (chunk == null || !chunk.id.equals("CVOX"))
                throw new IllegalArgumentException("Not a valid cvox file, it should start with the CVOX chunk");
            CvoxModel model = null;
            ColourMap cmap = new ColourMap();
            ColourMap vmap = new ColourMap();
            EL<Cube> cubes = new EL<>();
            EL<Cube> voxels = new EL<>();
            chunk = RiffReader.readNextChunk(dis);
            while (chunk != null) {
                switch (chunk.id) {
                    case "SIZE" -> {
                        if (model != null) {
                            model.fill(cmap, cubes);
                            model.fill(vmap, voxels);
                        }
                        model = new CvoxModel(readDimensionsFromSIZE(chunk));
                        res.add(model, readTranslationFromSIZE(chunk));
                    }
                    case "CMAP" -> cmap = readMap(chunk);
                    case "VMAP" -> vmap = readMap(chunk);
                    case "voxel.XYZ " -> voxels = readVoxels(chunk);
                    case "CUBE" -> cubes = readCubes(chunk);
                }
                chunk = RiffReader.readNextChunk(dis);
            }
            if (model != null) {
                model.fill(cmap, cubes);
                model.fill(vmap, voxels);
            }
            return res;
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static XYZ readDimensionsFromSIZE(Chunk chunk) {
        return readXYZFromBytes(chunk.content);
    }

    private static XYZ readTranslationFromSIZE(Chunk chunk) {
        byte[] translationX , translationY , translationZ;
        translationX = translationY = translationZ = new byte[4];
        System.arraycopy(chunk.content, 3, translationX, 0, 4);
        System.arraycopy(chunk.content, 7, translationY, 0, 4);
        System.arraycopy(chunk.content, 11, translationZ, 0, 4);
        return new XYZ(bytesToInt(translationX, true), bytesToInt(translationY, true), bytesToInt(translationZ, true));
    }

    private static ColourMap readMap(Chunk chunk) throws IllegalArgumentException {
        if (chunk.size % 7 != 0)
            throw new IllegalArgumentException(chunk.id + " is invalid as it has a byte count not divisible by 7");
        ColourMap res = new ColourMap();
        int bb = 0;
        byte[] colour = new byte[4];
        byte[] count = new byte[3];
        while (bb < chunk.content.length) {
            System.arraycopy(chunk.content, bb, colour, 0, 4);
            System.arraycopy(chunk.content, bb + 4, count, 0, 3);
            res.add(new Colour(Colour.rgbaToArgb(bytesToInt(colour))), bytesToInt(count, true, 3));
            bb += 7;
        }
        return res;
    }

    private static EL<Cube> readVoxels(Chunk chunk) {
        EL<Cube> res = new EL<>();
        for (int bb = 0; bb < chunk.size; bb += 3) {
            byte[] lowBytes = new byte[3];
            System.arraycopy(chunk.content, bb, lowBytes, 0, 3);
            XYZ low = readXYZFromBytes(lowBytes);
            res.add(new Cube(low, low));
        }
        return res;
    }

    private static EL<Cube> readCubes(Chunk chunk) {
        EL<Cube> res = new EL<>();
        for (int bb = 0; bb < chunk.size; bb += 6) {
            byte[] lowBytes = new byte[3];
            byte[] highBytes = new byte[3];
            System.arraycopy(chunk.content, bb, lowBytes, 0, 3);
            System.arraycopy(chunk.content, bb + 3, highBytes, 0, 3);
            XYZ low = readXYZFromBytes(lowBytes);
            XYZ high = readXYZFromBytes(highBytes);
            res.add(new Cube(low, high));
        }
        return res;
    }

    private static XYZ readXYZFromBytes(byte[] bytes) {
        int zero = 0; // used to read unsigned value from a signed byte
        return new XYZ(zero | bytes[0], zero | bytes[1], zero | bytes[2]);
    }
}
