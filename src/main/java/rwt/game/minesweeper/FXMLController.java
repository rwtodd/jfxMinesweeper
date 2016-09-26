/*
 * Copyright Richard Todd. I put the code under the
 * GPL v2.0.  See the LICENSE file in the repository.
 * for more information.
 */
package rwt.game.minesweeper;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javafx.animation.*;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class FXMLController implements Initializable {

    @FXML
    private Pane board;
    
    // a status text property to bind to FXML... 
    private StringProperty _statusTextProperty = new SimpleStringProperty("Ok.");
    public StringProperty statusTextProperty() { return _statusTextProperty; }
    public String getStatusText() { return _statusTextProperty.get(); }
    public void setStatusText(String s) { _statusTextProperty.set(s); }
    
    private final static int TILES = 20;
    private final static double PCTBOMBS = 0.1;
    
    // most of the work of this controller is coordinating the MineField with
    // its visual representation in TileBoxes...
    private MineField   mineField;
    private TileBox[][] mineFieldView;
    
    private void setupBoard() {
        board.getChildren().clear();
        board.getStyleClass().clear();
        Group g = new Group();
        
        mineField = new MineField(TILES, TILES, PCTBOMBS);
        _statusTextProperty.set("There are " + Integer.toString(mineField.howManyMines()) + " mines.");
        
        mineFieldView = new TileBox[TILES][TILES];
        
        final double height =  board.getHeight() / TILES;
        final double width = board.getWidth() / TILES;
        
        for(int y = 0; y < TILES; ++y) {
            for(int x = 0; x < TILES; ++x) {
                final TileBox b = new TileBox(width, height, mineField.countNeighbors(x, y));
                mineFieldView[x][y] = b;
                b.setPrefSize(width, height);
                b.setLayoutX(x*width);
                b.setLayoutY(y*height);
                b.setScaleShape(true);
                b.setSnapToPixel(false);
                b.getStyleClass().add("cell");
                final int theX = x; 
                final int theY = y;
                b.setOnMouseClicked( ev -> { 
                    if(ev.getButton() == MouseButton.SECONDARY) {
                        handleRightClick(theX, theY);
                    } else {
                        handleClick(theX, theY);
                    }
                });
                g.getChildren().add(b);                  
            }  
        }
        board.getChildren().add(g);
    }
    
    // if the only un-flipped tiles are bombs, you win!
    private void checkForWin() {
        for(int y = 0; y < mineFieldView[0].length; y++) {
            for(int x = 0; x < mineFieldView.length; x++) {
                if(!mineFieldView[x][y].hasFlipped() && 
                        !mineField.hasBomb(x, y)) {
                    return; // still more spots left!
                }
            }
        }
        
        // if we got here, you won!  Animate all the tiles endlessly...
        List<Transition> lst = new ArrayList<>();
        for(int y = 0; y < mineFieldView[0].length; y++) {
            for(int x = 0; x < mineFieldView.length; x++) {
                lst.add(mineFieldView[x][y].flip(Duration.ZERO));
            }
        }
        ParallelTransition endgame = new ParallelTransition();
        endgame.setCycleCount(4);
        endgame.getChildren().addAll(lst);
        setStatusText("You Win!");       
        endgame.play(); 
    }
    
    private void handleClick(final int x, final int y) { 
        if(mineFieldView[x][y].isFlagged()) return; // can't accidentally die!
        
        if(mineField.hasBomb(x, y)) {
            handleExplosion(x,y);
        } else {
            handleNormalClick(x,y);
            checkForWin();
        }   
    }

    private void handleRightClick(final int x, final int y) {
        mineFieldView[x][y].flag();
    }
    
    private void recursiveFlip(int x, int y, final int origX, final int origY, List<Transition> flips) {
        // done already if we've already been flipped...
        if(mineFieldView[x][y].hasFlipped()) return;

        // delay is relative to distance form origin...
        double dist = Math.sqrt((x - origX)*(x - origX)+(y-origY)*(y-origY));
        flips.add(mineFieldView[x][y].flip(Duration.seconds(dist*0.02)));

        // now, flip neighbors, unless we had a number showing...
        if(mineField.countNeighbors(x, y) > 0) return;
        
        int minx = Math.max(x-1,0);
        int maxx = Math.min(x+1,mineFieldView.length-1);
        int miny = Math.max(y-1,0);
        int maxy = Math.min(y+1,mineFieldView[0].length-1);
        for (y = miny; y <= maxy; ++y) {
           for (x = minx; x <= maxx; ++x) {
               recursiveFlip(x,y,origX,origY,flips);
           }
        }
     
    }
    private void handleNormalClick(final int x, final int y) {
        List<Transition> transitions = new ArrayList<>();
        recursiveFlip(x, y, x, y, transitions);
        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(transitions);
        pt.play();
    }
    
    private void handleExplosion(final int x, final int y) {
        board.getStyleClass().add("sploded");
        
        List<Animation> animations = new java.util.ArrayList<>();
        for(int mfy = 0; mfy < mineFieldView[0].length; ++mfy) {
            for(int mfx = 0; mfx < mineFieldView.length; ++mfx) {
                
                animations.add(mineFieldView[mfx][mfy].explode(mfx - x, mfy - y));
            }
        }        
        
        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(animations);
        pt.play();        
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::setupBoard);
    }
    
    @FXML
    private void btnExit(ActionEvent e) {
        Platform.exit();
    }
    
    @FXML
    private void btnNewGame(ActionEvent e) {
        setupBoard();
    }
}
