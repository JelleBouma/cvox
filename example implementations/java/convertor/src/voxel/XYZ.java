package voxel;

import utils.EL;
import static utils.NumberUtilities.encroach;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class XYZ {
    public static final XYZ MIN_VALUE = new XYZ(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    public static final XYZ MAX_VALUE = new XYZ(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final XYZ ZERO = new XYZ();
    public static final XYZ ONE = new XYZ(1);
    public int x;
    public int y;
    public int z;

    public XYZ(int i) {
        this(i, i, i);
    }

    public XYZ() {
        this(0, 0, 0);
    }

    public XYZ(byte x, byte y, byte z) {
        this(x & 0xff, y & 0xff, z & 0xff);
    }

    public XYZ(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get x, y, or z respectively by their index.
     * Overflow is supported and will evaluate to modulo 3.
     * For example: index 3 evaluates to 0 and as such will return x.
     */
    public int get(int index) {
        int moddedIndex = index % 3;
        return switch (moddedIndex) {
            case 0 -> x;
            case 1 -> y;
            default -> z;
        };
    }

    /**
     * Set a value to x, y, or z respectively by their index.
     * Overflow is supported and will evaluate to modulo 3.
     * For example: index 3 evaluates to 0 and as such will set x.
     */
    public void set(int index, int value) {
        int moddedIndex = index % 3;
        switch (moddedIndex) {
            case 0 -> x = value;
            case 1 -> y = value;
            default -> z = value;
        }
    }

    /**
     * Both bounds are inclusive.
     */
    public boolean withinBounds(XYZ bound0, XYZ bound1) {
        XYZ lower = bound0.min(bound1);
        XYZ upper = bound0.max(bound1);
        return !hasLesserThan(lower) && !hasGreaterThan(upper);
    }

    public int sum() {
        return x + y + z;
    }

    public XYZ abs() {
        return transform(Math::abs);
    }

    public XYZ add(XYZ toAdd) {
        return combine(toAdd, Math::addExact);
    }

    public XYZ subtract(XYZ toSub) {
        return combine(toSub,Math::subtractExact);
    }

    public XYZ min(XYZ toMin) {
        return combine(toMin, Math::min);
    }

    public XYZ max(XYZ toMax) {
        return combine(toMax, Math::max);
    }

    public XYZ mult(XYZ toMult) {
        return combine(toMult, Math::multiplyExact);
    }

    /**
     * divide by, rounded down
     */
    public XYZ div(int divideBy) {
        return div(new XYZ(divideBy, divideBy, divideBy));
    }

    /**
     * divide by, rounded down
     */
    public XYZ div(XYZ divideBy) {
        return combine(divideBy, Math::divideExact);
    }

    public XYZ difference(XYZ toDiff) {
        return subtract(toDiff).abs();
    }

    public XYZ greaterThan(XYZ lesser) {
        return combine(lesser, (g, l) -> g > l ? 1 : 0);
    }

    public XYZ lesserThan(XYZ lesser) {
        return combine(lesser, (g, l) -> g < l ? 1 : 0);
    }

    public boolean has(Predicate<Integer> predicate) {
        return predicate.test(x) || predicate.test(y) || predicate.test(z);
    }

    public boolean hasLesserThan(XYZ greater) {
        return lesserThan(greater).sum() > 0;
    }

    public boolean hasGreaterThan(XYZ lesser) {
        return greaterThan(lesser).sum() > 0;
    }
    public XYZ combine(XYZ xyz, BiFunction<Integer, Integer, Integer> combiner) {
        return new XYZ(combiner.apply(x, xyz.x), combiner.apply(y, xyz.y), combiner.apply(z, xyz.z));
    }

    public XYZ transform(Function<Integer, Integer> transformer) {
        return new XYZ(transformer.apply(x), transformer.apply(y), transformer.apply(z));
    }
    // from inclusive, to exclusive
    public void fromTo(XYZ to, Consumer<XYZ> consumer) {
        for (int xx = x; xx != to.x; xx = encroach(xx, to.x))
            for (int yy = y; yy != to.y; yy = encroach(yy, to.y))
                for (int zz = z; zz != to.z; zz = encroach(zz, to.z))
                    consumer.accept(new XYZ(xx, yy, zz));
    }

    // from inclusive, to exclusive
    public EL<XYZ> fromToList(XYZ to, XYZ step) {
        EL<XYZ> res = new EL<>();
        for (int xx = x; xx != to.x; xx = encroach(xx, to.x, step.x))
            for (int yy = y; yy != to.y; yy = encroach(yy, to.y, step.y))
                for (int zz = z; zz != to.z; zz = encroach(zz, to.z, step.z)) {
                    res.add(new XYZ(xx, yy, zz));
                }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == XYZ.class && ((XYZ) obj).x == x && ((XYZ) obj).y == y && ((XYZ) obj).z == z;
    }

    @Override
    public String toString() {
        return "voxel.XYZ{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public XYZ clone() {
        return new XYZ(x, y, z);
    }
}
