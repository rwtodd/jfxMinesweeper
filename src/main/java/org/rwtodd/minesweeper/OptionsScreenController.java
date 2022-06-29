/*
 * Copyright Richard Todd. I put the code under the
 * MIT License
 */
package org.rwtodd.minesweeper;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author richa
 */
public class OptionsScreenController {

    private boolean wantsNewGame;  // did the user press "New Game"?
    @FXML private Slider rows, cols, bombs;
    @FXML private Button ngButton;
    
    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        rows.valueProperty().addListener((obs, oldval, newVal) ->
          rows.setValue(Math.round(newVal.doubleValue())));
        cols.valueProperty().addListener((obs, oldval, newVal) ->
          cols.setValue(Math.round(newVal.doubleValue())));
        bombs.valueProperty().addListener((obs, oldval, newVal) ->
          bombs.setValue(Math.round(newVal.doubleValue())));
        wantsNewGame = false;
    }    

    public void focusOnButton() {
        ngButton.requestFocus();
    }
    
    @FXML private void okBtn(ActionEvent e) { 
        wantsNewGame = true;
        ((Stage)bombs.getScene().getWindow()).close(); // My eyes!  They burn!
    }    
    
    public void initValues(int nCols, int nRows, double nBombs) {
        rows.setValue(nRows);
        cols.setValue(nCols);
        bombs.setValue(nBombs * 100);
    }
    
    public boolean newGameRequested() { return wantsNewGame; }
    public int getRows() { return (int)(rows.getValue()); }
    public int getCols() { return (int)(cols.getValue()); }
    public double getPctBombs() { return bombs.getValue() / 100.0; }
}
