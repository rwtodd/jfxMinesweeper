/*
 * Copyright Richard Todd. I put the code under the
 * MIT License.
 */
package rwt.minesweeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FXMLController {

    @FXML private Pane board;
    @FXML private StackPane overlays;
    
    // a status text property to bind to FXML... 
    private StringProperty _statusTextProperty = new SimpleStringProperty("Ok.");
    public StringProperty statusTextProperty() { return _statusTextProperty; }
    public String getStatusText() { return _statusTextProperty.get(); }
    public void setStatusText(String s) { _statusTextProperty.set(s); }
    
    // Here's the state we care about...
    private int rowTiles = 20;
    private int colTiles = 20;
    private double pctBombs = 0.1;
    
    // most of the work of this controller is coordinating the MineField with
    // its visual representation in TileBoxes...
    private MineField   mineField;
    private TileBox[][] mineFieldView;
    
    public void resize() {
        
        final double height =  board.getHeight() / rowTiles;
        final double width = board.getWidth() / colTiles;

        for(int y = 0; y < rowTiles; ++y) {
            for(int x = 0; x < colTiles; ++x) {
                TileBox b = mineFieldView[x][y];
                b.setNewSize(width,height);
                b.setPrefSize(width, height);
                b.setLayoutX(x*width);
                b.setLayoutY(y*height);
            }
        }
    }
    
    private void setupBoard() {
        // some initial bookkeeping...
        overlays.getChildren().clear();
        board.getChildren().clear();
        board.getStyleClass().clear();
                
        mineField = new MineField(colTiles, rowTiles, pctBombs);
        _statusTextProperty.set("There are " + Integer.toString(mineField.howManyMines()) + " mines.");
        
        mineFieldView = new TileBox[colTiles][rowTiles];
        
        final double height =  board.getHeight() / rowTiles;
        final double width = board.getWidth() / colTiles;
        
        for(int y = 0; y < rowTiles; ++y) {
            for(int x = 0; x < colTiles; ++x) {
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
                board.getChildren().add(b);                  
            }  
        }
        overlays.getChildren().add(board);
        Platform.runLater(System::gc);
    }
    
    // if the only un-flipped tiles are bombs, you win!
    private void checkForWin() {
        for(int y = 0; y < rowTiles; y++) {
            for(int x = 0; x < colTiles; x++) {
                if(!mineFieldView[x][y].hasFlipped() && 
                        !mineField.hasBomb(x, y)) {
                    return; // still more spots left!
                }
            }
        }
        
        // if we got here, you won!  Animate all the tiles endlessly...
        List<Transition> lst = new ArrayList<>();
        for(int y = 0; y < rowTiles; y++) {
            for(int x = 0; x < colTiles; x++) {
                lst.add(mineFieldView[x][y].flip(Duration.ZERO));
            }
        }
        ParallelTransition endgame = new ParallelTransition();
        endgame.setCycleCount(4);
        endgame.getChildren().addAll(lst);
        setStatusText("You Win!");       
        endgame.play();
        Label winner = new Label("You Win!!!");
        winner.setTranslateZ(-50.0);
        winner.setFont(Font.font(40));
        winner.setTextFill(Color.GREEN);
        winner.setPadding(new Insets(15));
        winner.setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW,null,null)));
        overlays.getChildren().add(winner);
    }
    
    private void handleClick(final int x, final int y) { 
        if(mineFieldView[x][y].isFlagged()) return; // can't accidentally die!
        
        if(mineField.hasBomb(x, y)) {
            handleExplosion(x,y);
        } else {
            handleNormalClick(x,y);
            checkForWin();
        }
        Platform.runLater(System::gc);
    }

    private void handleRightClick(final int x, final int y) {
        mineFieldView[x][y].flag();
    }
    
    private void recursiveFlip(int x, int y, final int origX, final int origY, List<Transition> flips) {
        // done already if we've already been flipped...
        if(mineFieldView[x][y].hasFlipped()) return;

        // delay is relative to distance form origin...
        double dist = Math.sqrt((x - origX)*(x - origX)+(y-origY)*(y-origY));
        flips.add(mineFieldView[x][y].flip(Duration.seconds(dist*0.04)));

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
        for(int mfy = 0; mfy < rowTiles; ++mfy) {
            for(int mfx = 0; mfx < colTiles; ++mfx) {
                
                animations.add(mineFieldView[mfx][mfy].explode(mfx - x, mfy - y));
            }
        }        
        
        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(animations);
        pt.play();        
    }

    // pressing the 'n' key starts a new game....    
    @FXML
    private void keyTyped(javafx.scene.input.KeyEvent kev) {
        if ("n".equals(kev.getCharacter())) {
            kev.consume();
            if (board.getStyleClass().contains("sploded")) {
                // if the game was over, start a new game...
                setupBoard();
            } else {
                // if the game isn't over, pull up the options in case they
                // pressed the key by accident...
                try {
                    btnOptions(null);
                } catch (Exception e) {
                    System.err.println(e.toString());
                }
            }
        }

    }
    
    @FXML
    public void initialize() {
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
    
    @FXML
    private void btnOptions(ActionEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/OptionsScreen.fxml"));
        Parent root = fxmlLoader.load();
        OptionsScreenController controller = fxmlLoader.getController();
        controller.initValues(colTiles, rowTiles, pctBombs);
        
        Scene scene = new Scene(root);
        Stage st = new Stage(StageStyle.DECORATED);
        controller.focusOnButton();
        st.setTitle("Options");
        st.initOwner(board.getScene().getWindow());
        st.initModality(Modality.APPLICATION_MODAL);
        st.setScene(scene);
        st.sizeToScene();
        st.setResizable(false);
        st.showAndWait();
        if (controller.newGameRequested()) {
            colTiles = controller.getCols();
            rowTiles = controller.getRows();
            pctBombs = controller.getPctBombs();
            setupBoard();
        }
    }
}
