package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import javafx.geometry.Point2D;

import java.io.IOException;


/**
 * Manages the display and the interaction with the basemap
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class BaseMapController {
    private static final double SIZE_TILE = 256;
    private final SimpleObjectProperty<Point2D> lastMousePosition;
    private final TileManager tileManager;
    private final GraphicsContext graphicsContext;
    private final MapParameters mapParameters;
    private final Canvas canvas;
    private final Pane pane;
    private boolean redrawNeeded;

    /**
     * Constructs a basemap controller
     *
     * @param tileManager   tile manager
     * @param mapParameters parameters of the map
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        this.lastMousePosition = new SimpleObjectProperty<>();
        this.graphicsContext = canvas.getGraphicsContext2D();
        addListeners();
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }

    /**
     * Returns the pane containing the map
     *
     * @return the pane containing the map
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Centers the map on the given position
     *
     * @param position on the map
     */
    public void centerOn(GeoPos position) {
        Point2D center = new Point2D(mapParameters.getMinX() + canvas.getWidth() / 2,
                mapParameters.getMinY() + canvas.getHeight() / 2);
        Point2D newCenter = new Point2D(
                WebMercator.x(mapParameters.getZoom(), position.longitude()),
                WebMercator.y(mapParameters.getZoom(), position.latitude()));
        Point2D distance = newCenter.subtract(center);
        mapParameters.scroll(distance.getX(), distance.getY());
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); /// index 0

        int zoom = mapParameters.getZoom();
        int xTileID = mapTileCoordinate(mapParameters.getMinX());
        int yTileID = mapTileCoordinate(mapParameters.getMinY());
        double xTilePos = xTileID * SIZE_TILE - mapParameters.getMinX();
        double yTilePos = yTileID * SIZE_TILE - mapParameters.getMinY();
        for (int sizeTileY = 0; sizeTileY < canvas.getHeight() - yTilePos; sizeTileY += SIZE_TILE) {
            for (int sizeTileX = 0; sizeTileX < canvas.getWidth() - xTilePos; sizeTileX += SIZE_TILE) {
                if (TileManager.TileId.isValid(zoom, xTileID, yTileID)) {
                    int xTileAdd = (int) (sizeTileX / SIZE_TILE);
                    try {
                        graphicsContext.drawImage(
                                tileManager.imageForTileAt(
                                        new TileManager.TileId(zoom, xTileID + xTileAdd, yTileID)),
                                sizeTileX + xTilePos,
                                sizeTileY + yTilePos);

                    } catch (IOException ignored) {
                    }
                }
            }
            yTileID++;
        }
    }

    private void addListeners() {

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawOnNextPulse());

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            mapParameters.scroll(e.getX(), e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-e.getX(), -e.getY());

            redrawOnNextPulse();

        });

        pane.setOnMousePressed(event -> lastMousePosition.set(new Point2D(event.getX(), event.getY())));

        mapParameters.minXProperty().addListener(observable -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(observable -> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(observable -> redrawOnNextPulse());

        pane.setOnMouseDragged(event -> {
            Point2D currentPos = new Point2D(event.getX(), event.getY());
            Point2D newPOs = lastMousePosition.getValue().subtract(currentPos);
            mapParameters.scroll(newPOs.getX(), newPOs.getY());
            lastMousePosition.set(currentPos);
        });

        canvas.sceneProperty().
                addListener((p, oldS, newS) -> {
                    assert oldS == null;
                    newS.addPreLayoutPulseListener(this::redrawIfNeeded);
                });
    }

    private int mapTileCoordinate(double c) {
        return (int) Math.floor(c / SIZE_TILE);
    }
}
