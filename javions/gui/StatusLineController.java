package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Manages the state line
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class StatusLineController {
    private final BorderPane pane;
    private final IntegerProperty integerProperty;
    private final LongProperty longProperty;

    /**
     * Constructs a state line that shows the number of aircraft visible and the number of messages received
     */
    public StatusLineController() {
        this.longProperty = new SimpleLongProperty();
        this.integerProperty = new SimpleIntegerProperty();
        Text leftText = new Text();
        Text rightText = new Text();
        pane = new BorderPane();
        pane.getStylesheets().add("status.css");
        pane.setLeft(leftText);
        pane.setRight(rightText);
        leftText.textProperty().bind(Bindings.format("Aéronefs visibles : %d", integerProperty));
        rightText.textProperty().bind(Bindings.format("Messages reçus : %d", longProperty));
    }

    /**
     * Returns the pane containing the state line
     *
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Returns the property that contains the number currently of visible aircraft
     *
     * @return the integer property
     */

    public IntegerProperty aircraftCountProperty() {
        return integerProperty;
    }

    /**
     * Returns the property that contains the number of currently of messages
     *
     * @return the long property
     */

    public LongProperty messageCountProperty() {
        return longProperty;
    }
}
