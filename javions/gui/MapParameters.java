package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * Represents the portion of the visible map
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class MapParameters {

    private final IntegerProperty zoom;
    private final DoubleProperty minX;
    private final DoubleProperty minY;
    private static final int MIN_VALUE_ZOOM = 6;
    private static final int MAX_VALUE_ZOOM = 19;

    /**
     * @param zoom level of zoom in map
     * @param minX coordinate of the left above border of the visible map
     * @param minY coordinate of the left above border of the visible map
     * @throws IllegalArgumentException if the given zoom is not in the limits mentioned
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(MIN_VALUE_ZOOM <= zoom && zoom <= MAX_VALUE_ZOOM);
        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);
        }

    /**
     * Returns a read only integer property
     *
     * @return a read only integer property of the given zoom level
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }

    /**
     * Returns a read only integer property
     *
     * @return a read only integer property of the given x coordinates of the visible map
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    /**
     * Reads only integer property
     *
     * @return a read only integer property of the given y coordinates of the visible map
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minY;
    }

    /**
     * Returns the value of zoom level
     *
     * @return value of the zoom level
     */
    public int getZoom() {
        return zoom.get();
    }

    /**
     * Returns the value of property coordinate x of the visible map
     *
     * @return value
     */
    public double getMinX() {
        return minX.get();
    }

    /**
     * Returns the value of property coordinate yx of the visible map
     *
     * @return value
     */
    public double getMinY() {
        return minY.get();
    }

    /**
     * Translates the coordinates of position visible map given x and y and obtain new coordinates.
     *
     * @param x to be added to the current coordinate of the visible map
     * @param y to be added to the current coordinate of the visible map
     */
    public void scroll(double x, double y) {
        double newVisiblePositionX = minX.get() + x;
        double newVisiblePositionY = minY.get() + y;
        minX.set(newVisiblePositionX);
        minY.set(newVisiblePositionY);
    }

    /**
     * Changes the current zoom level and update it with coordinates x and y to obtained new zoom level
     *
     * @param deltaZoomLevel zoom level difference to add to the current zoom level
     */
    public void changeZoomLevel(int deltaZoomLevel) {
        int currentZoomLevel = getZoom();
        int nextZoomLevel = Math2.clamp(MIN_VALUE_ZOOM, getZoom() + deltaZoomLevel, MAX_VALUE_ZOOM);
        if (nextZoomLevel != currentZoomLevel) {
            double xInZoomLevel = Math.scalb(getMinX(), deltaZoomLevel);
            double yInZoomLevel = Math.scalb(getMinY(), deltaZoomLevel);
            zoom.set(nextZoomLevel);
            minX.set(xInZoomLevel);
            minY.set(yInZoomLevel);
        }
    }
}
