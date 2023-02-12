/*
 * Copyright (C) 2020 Jelle Bouma
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package voxel;

import java.awt.*;

/**
 * @author Jelle Bouma
 */
public class Colour extends Color {

    public static Colour BLACK = new Colour(0, 0, 0);

    public Colour(int rgb) {
        super(rgb);
    }

    public Colour(int argb, boolean hasAlpha) {
        super(argb, hasAlpha);
    }

    public Colour(int r, int g, int b) {
        super(r, g, b);
    }

    public static int rgbaToArgb(int rgba) {
        return (rgba >>> 8) | (rgba << (32-8));
    }

    public static int argbToRgba(int argb) {
        return (argb << 8) | (argb >>> (32-8));
    }
}
