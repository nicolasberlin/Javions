package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a manager of OSM tiles. It gets tiles from a tile server and store them in the memory cache and in the disk cache
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class TileManager {
    private static final String FORMAT = ".png";
    private static final int CAPACITY_OF_CACHE_MEMORY = 100;
    Path path;
    String server;

    private final LinkedHashMap<TileId, Image> cacheMemory = new LinkedHashMap<>(
            CAPACITY_OF_CACHE_MEMORY, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<TileId, Image> eldest) {
            return size() > CAPACITY_OF_CACHE_MEMORY;
        }
    };

    /**
     * Constructs a tile manager
     *
     * @param path   of the fold having the disk cache
     * @param server of the tile
     */
    public TileManager(Path path, String server) {
        this.path = path;
        this.server = server;
    }

    /**
     * Takes an tileID and returns its image
     *
     * @param tileId of the tile
     * @return the image of the titleId
     * @throws IOException in case of input/output error
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        if (!cacheMemory.containsKey(tileId)) {
            Path pathSpecified = path(tileId.zoom(), tileId.x(), tileId.y());
            return downloadImageToDiskCacheOrCacheMemory(tileId, pathSpecified);
        } else {
            return cacheMemory.get(tileId);
        }
    }

    private Path path(int zoom, int x, int y) {
        return path.resolve(Integer.toString(zoom))
                .resolve(Integer.toString(x))
                .resolve(y + FORMAT);
    }

    private Image downloadImageToDiskCacheOrCacheMemory(TileId tileId, Path specifiedPath) throws IOException {

        if (Files.exists(specifiedPath)) {
            InputStream pathStream = new FileInputStream(specifiedPath.toFile());
            cacheMemory.put(tileId, new Image(pathStream));
        } else {
            String pathString = "https://" + server + "/" +
                    tileId.zoom() + "/" + tileId.x() + "/" + tileId.y() + FORMAT;
            URL url = new URL(pathString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Javions");
            Files.createDirectories(specifiedPath.getParent());
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream out = new FileOutputStream(specifiedPath.toFile())) {
                byte[] bytes = inputStream.readAllBytes();
                out.write(bytes);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                Image newImage = new Image(byteArrayInputStream);
                cacheMemory.put(tileId, newImage);
            }
        }
        return cacheMemory.get(tileId);
    }

    /**
     * Represents an OSM tile
     *
     * @param zoom of the tile
     * @param x    index of the tile
     * @param y    index of the tile
     * @author Ünlüer Asli (329696)
     * @author Berlin Nicolas (355535)
     */
    public record TileId(int zoom, int x, int y) {
        private final static int MINIMUM_ZOOM_LEVEL = 0;
        private final static int MAXIMUM_ZOOM_LEVEL = 19;

        /**
         * Returns true if and only if the zoom, the index X and the index Y represent a valid tile
         *
         * @param zoom of the tile
         * @param x    index of the tile
         * @param y    index of the tile
         * @return true if it is a valid tile, otherwise false
         */
        public static boolean isValid(int zoom, int x, int y) {
            if (MINIMUM_ZOOM_LEVEL <= zoom && zoom <= MAXIMUM_ZOOM_LEVEL) {
                int maxIndexN = 1 << zoom;
                return (0 <= x && x < maxIndexN && 0 <= y && y < maxIndexN);
            }
            return false;
        }
    }
}

