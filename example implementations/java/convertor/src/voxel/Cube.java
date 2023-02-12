package voxel;

public class Cube {
    public XYZ low; //inclusive
    public XYZ high; //inclusive
    public Colour colour = null;

    public Cube(XYZ low, XYZ high) {
        this.low = low;
        this.high = high;
    }

    public Cube(XYZ low, XYZ high, Colour c) {
        this(low, high);
        this.colour = c;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Cube other && other.low.equals(low) && other.high.equals(high);
    }

    @Override
    public Cube clone() {
        return new Cube(low.clone(), high.clone(), colour);
    }
}
