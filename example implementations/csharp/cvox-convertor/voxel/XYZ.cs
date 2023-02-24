using static cvox_convertor.utils.NumberUtilities;

namespace cvox_convertor.voxel
{
    public class XYZ
    {
        public static readonly XYZ MIN_VALUE = new(int.MinValue, int.MinValue, int.MinValue);
        public static readonly XYZ MAX_VALUE = new(int.MaxValue, int.MaxValue, int.MaxValue);
        public static readonly XYZ ZERO = new();
        public int X;
        public int Y;
        public int Z;

        public XYZ(int i) => (X, Y, Z) = (i, i, i);

        public XYZ() => (X, Y, Z) = (0, 0, 0);

        public XYZ(byte x, byte y, byte z) => (X, Y, Z) = (x & 0xff, y & 0xff, z & 0xff);

        public XYZ(int x, int y, int z) => (X, Y, Z) = (x, y, z);

        public static bool operator ==(XYZ xyz0, XYZ xyz1)
        {
            return xyz0.X == xyz1.X && xyz0.Y == xyz1.Y && xyz0.Z == xyz1.Z;
        }

        public static bool operator !=(XYZ xyz0, XYZ xyz1)
        {
            return !(xyz0 == xyz1);
        }

        public static XYZ operator +(XYZ xyz0, XYZ xyz1)
        {
            return xyz0.Combine(xyz1, (a, b) => a + b);
        }

        public static XYZ operator -(XYZ xyz0, XYZ xyz1)
        {
            return xyz0.Combine(xyz1, (a, b) => a - b);
        }

        public static XYZ operator -(XYZ xyz0, int subtraction)
        {
            return xyz0.Transform(a => a - subtraction);
        }

        public static XYZ operator *(XYZ xyz0, XYZ xyz1)
        {
            return xyz0.Combine(xyz1, (a, b) => a * b);
        }

        public static XYZ operator /(XYZ xyz0, XYZ xyz1)
        {
            return xyz0.Combine(xyz1, (a, b) => a / b);
        }

        public static XYZ operator /(XYZ xyz, int divideBy)
        {
            return xyz.Transform(a => a / divideBy);
        }

        public static XYZ operator %(XYZ xyz0, XYZ xyz1)
        {
            return xyz0.Combine(xyz1, (a, b) => a % b);
        }

        public static XYZ operator %(XYZ xyz, int modBy)
        {
            return xyz.Transform(a => a % modBy);
        }

        /**
         * Get x, y, or z respectively by their index.
         * Overflow is supported and will evaluate to modulo 3.
         * For example: index 3 evaluates to 0 and as such will return x.
         */
        public int Get(int index)
        {
            int moddedIndex = index % 3;
            switch (moddedIndex)
            {
                case 0:
                    return X;
                case 1:
                    return Y;
                default:
                    return Z;
            };
        }

        /**
         * Set a value to x, y, or z respectively by their index.
         * Overflow is supported and will evaluate to modulo 3.
         * For example: index 3 evaluates to 0 and as such will set x.
         */
        public void Set(int index, int value)
        {
            int moddedIndex = index % 3;
            switch (moddedIndex)
            {
                case 0:
                    X = value;
                    break;
                case 1:
                    Y = value;
                    break;
                default:
                    Z = value;
                    break;
            }
        }

        /**
         * Both bounds are inclusive.
         */
        public bool WithinBounds(XYZ bound0, XYZ bound1)
        {
            XYZ lower = bound0.Min(bound1);
            XYZ upper = bound0.Max(bound1);
            return !HasLesserThan(lower) && !HasGreaterThan(upper);
        }

        public int Sum()
        {
            return X + Y + Z;
        }

        public XYZ Abs()
        {
            return Transform(Math.Abs);
        }

        public XYZ Min(XYZ toMin)
        {
            return Combine(toMin, Math.Min);
        }

        public XYZ Max(XYZ toMax)
        {
            return Combine(toMax, Math.Max);
        }

        public XYZ Difference(XYZ toDiff)
        {
            return (this - toDiff).Abs();
        }

        public XYZ GreaterThan(XYZ lesser)
        {
            return Combine(lesser, (g, l) => g > l ? 1 : 0);
        }

        public XYZ LesserThan(XYZ lesser)
        {
            return Combine(lesser, (g, l) => g < l ? 1 : 0);
        }

        public bool Has(Predicate<int> predicate)
        {
            return predicate(X) || predicate(Y) || predicate(Z);
        }

        public bool HasLesserThan(XYZ greater)
        {
            return LesserThan(greater).Sum() > 0;
        }

        public bool HasGreaterThan(XYZ lesser)
        {
            return GreaterThan(lesser).Sum() > 0;
        }
        public XYZ Combine(XYZ xyz, Func<int, int, int> combiner)
        {
            return new XYZ(combiner(X, xyz.X), combiner(Y, xyz.Y), combiner(Z, xyz.Z));
        }

        public XYZ Transform(Func<int, int> transformer)
        {
            return new XYZ(transformer(X), transformer(Y), transformer(Z));
        }
        // from inclusive, to exclusive
        public void FromTo(XYZ to, Action<XYZ> consumer)
        {
            for (int xx = X; xx != to.X; xx = Encroach(xx, to.X))
                for (int yy = Y; yy != to.Y; yy = Encroach(yy, to.Y))
                    for (int zz = Z; zz != to.Z; zz = Encroach(zz, to.Z))
                        consumer(new XYZ(xx, yy, zz));
        }
    }
}
