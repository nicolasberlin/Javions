package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

/**
 * Represents a color gradient
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class ColorRamp {

    private final Color[] color;

    /**
     * Attribut which represents of the color gradient.
     */
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));

    /**
     * Constructs a color
     *
     * @param color sequence of colors, it can be also an array or a list
     * @throws IllegalArgumentException if the constructor does not contain at least two color.
     */
    public ColorRamp(Color... color) {
        Preconditions.checkArgument(color.length >= 2);
        this.color = color.clone();

    }

    /**
     * Returns a color from the given sequence or a new color which is taken from the two color where the colorValue
     * is situated.
     *
     * @param colorValue a double value of a color
     * @return if colorValue is negative it returns first element in the sequence if is it greater or equals to 1,
     * it returns the last element otherwise, if colorValue situated between the two color at given sequence,
     * it mixed two color and returns new color.
     */
    public Color at(double colorValue) {
        if (colorValue <= 0) {
            return color[0];
        } else if (colorValue >= 1) {
            return color[color.length - 1];
        }

        double realValue = colorValue * (color.length - 1);
        int roundDown = (int) Math.floor(realValue);
        int roundUp = roundDown + 1;
        Color previousColor = color[roundDown];
        Color nextColor = color[roundUp];
        return previousColor.interpolate(nextColor, realValue - roundDown);

    }
}
