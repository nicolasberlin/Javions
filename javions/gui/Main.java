package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *  Contains the main programme which means that we show the received messages and aircrafts in our pane.
 *  @author Ünlüer Asli (329696)
 *  @author Berlin Nicolas (355535)
 */

public final class Main extends Application {

    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final long CONVERSION_FROM_MILLI_TO_NANO_SECONDES = 1_000_000L;
    private static final int ZOOM_LEVEL = 8;
    private static final int MINX = 33530;
    private static final int MINY = 23070;
    private static final int FIRST_ELEMENT = 0;

    private static final double PRECISION = 1e+9;
    private final ConcurrentLinkedQueue<RawMessage> messages = new ConcurrentLinkedQueue<>();

    /**
     * Call the method launch so launches the main program
     * @param args
     */

    public static void main(String[] args){
        launch(args);
    }

    /**
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String file = Path.of(dbUrl.toURI()).toString();
        AircraftDatabase dataBase = new AircraftDatabase(file);

        MapParameters mapParameters = new MapParameters(ZOOM_LEVEL, MINX, MINY);
        TileManager tiles = new TileManager(Path.of("tile-cache"), "tile.openstreetmap.org");
        BaseMapController mapController = new BaseMapController(tiles, mapParameters);

        AircraftStateManager aircraftStateManager = new AircraftStateManager(dataBase);
        ObjectProperty<ObservableAircraftState> observableAircraftState = new SimpleObjectProperty<>();
        AircraftController  controller = new AircraftController(
                mapParameters, aircraftStateManager.states(), observableAircraftState);
        AircraftTableController tableController = new AircraftTableController(
                aircraftStateManager.states(), observableAircraftState);



        StatusLineController lineController = new StatusLineController();

        lineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));

        StackPane root = new StackPane(mapController.pane(), controller.pane());

        BorderPane borderPane = new BorderPane(tableController.pane());
        borderPane.setTop(lineController.pane());

        tableController.setOnDoubleClick(e -> mapController.centerOn(e.getPosition()));

        SplitPane mainPane = new SplitPane(root, borderPane);
        mainPane.setOrientation(Orientation.VERTICAL);

        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(MIN_WIDTH); //root
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();

        Parameters parameter = getParameters();
        List<String> rawParameter = parameter.getRaw();
        Thread thread;
        if(rawParameter.isEmpty()){ // getParametres ıcı
            AdsbDemodulator demodulator = new AdsbDemodulator(System.in);
            thread = new Thread(() -> {
                while(true){
                    try {
                        RawMessage rawMessage; //voır ma versıon
                        if ((rawMessage = demodulator.nextMessage()) != null) {
                            messages.add(rawMessage);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        }else{
            thread = new Thread(() -> {
                long startTime = System.nanoTime();
                String firstFile = rawParameter.get(FIRST_ELEMENT);
                readAllMessages(startTime, firstFile); // methode prefer ela mıenne
            });

        }
        thread.setDaemon(true);
        thread.start();

//si on fait un iterator avec les threads ça ne va pas marcher parce que iterateur va iterer la collection qui a deja rempli mais
        //puisque au debut de notre programme et au cours du temps la queue est rempli au debut il va etre nul et c'est pour cela
        //que il ne va pas iterer sur une queue vide.

        new AnimationTimer(){
            private long lastTime = 0;
            @Override
            public void handle(long now){
                try {
                    long messageRead = lineController.messageCountProperty().get();
                    while(!messages.isEmpty()){
                        RawMessage firstMessage = messages.poll();
                        if(Objects.nonNull(firstMessage)){
                            Message parsedMessage = MessageParser.parse(firstMessage);
                            if (parsedMessage != null) {
                                messageRead++;
                                aircraftStateManager.updateWithMessage(parsedMessage);

                            }
                                lineController.messageCountProperty().set(messageRead);
                            if(now - lastTime >= PRECISION) {
                                aircraftStateManager.purge();
                                lastTime = now;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    private void readAllMessages(long startTime, String firstFile) {
        try(DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(firstFile)))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while(true){
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rawMessage = new RawMessage(timeStampNs, new ByteString(bytes));
                long elapsedTime = rawMessage.timeStampNs() - (System.nanoTime() - startTime);
                if(elapsedTime > 0 ){
                    Thread.sleep((elapsedTime) /CONVERSION_FROM_MILLI_TO_NANO_SECONDES);
                }
                messages.add(rawMessage); //vérifier

            }
        }catch (IOException | InterruptedException e) {
            throw new Error(e);
        }
    }
}





