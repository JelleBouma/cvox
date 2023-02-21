using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cvox_convertor.utils
{
    public static class Extensions
    {
        /**
         * @param predicate The predicate to check for.
         * @return The first element (counting up from index 0) for which the predicate evaluates to true.
         * If the predicate is true for no element, null is returned instead.
         */
        public static E FirstMatch<E>(this List<E> list, Predicate<E> predicate)
        {
            foreach (E e in list)
                if (predicate(e))
                    return e;
            return default(E);
        }

        /**
         * @param predicate The predicate to check for.
         * @return A new list containing all and only elements for which the predicate evaluates to true.
         * If the predicate is true for no element, an empty list is returned.
         */
        public static List<E> AllMatches<E>(this List<E> list, Predicate<E> predicate)
        {
            List<E> res = new();
            foreach (E e in list)
                if (predicate(e))
                    res.Add(e);
            return res;
        }

        public static bool AnyMatch<E>(this List<E> list, Predicate<E> predicate)
        {
            foreach (E e in list)
                if (predicate(e))
                    return true;
            return false;
        }

        public static int ToRgba(this Color colour) {
            uint argb = (uint)colour.ToArgb();
            return (int)((argb << 8) | (argb >> 24));
        }
    }
}
