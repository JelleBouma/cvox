package voxel;

import static utils.NumberUtilities.bytesToInt;

import java.util.Arrays;

public class Palette {
    public static final int SIZE = 255;
    public Colour[] palette = new Colour[SIZE];

    public Palette() {
        for (int cc = 0; cc < SIZE; cc++)
            palette[cc] = Colour.BLACK;
    }

    public Palette(byte[] paletteBytes) {
        for (int cc = 0; cc < SIZE; cc++)
            palette[cc] = new Colour(Colour.rgbaToArgb(bytesToInt(Arrays.copyOfRange(paletteBytes, cc * 4, cc * 4 + 4))), true);
    }

    public Colour getColour(int i) {
        return palette[i - 1];
    }
    public void setColour(int i, Colour colour) {
        palette[i - 1] = colour;
    }
    public Colour[] getArray() {
        return palette;
    }
}
