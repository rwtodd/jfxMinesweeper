/*
 * Copyright Richard Todd. I put the code under the
 * GPL v2.0.  See the LICENSE file in the repository.
 * for more information.
 */

package rwt.game.minesweeper;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Camera;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Scene.fxml"));
        Parent root = fxmlLoader.load();
        FXMLController controller = fxmlLoader.getController();
        
        Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
        scene.getStylesheets().add("/styles/Styles.css");

        ChangeListener<? super java.lang.Number> cl = (val, ov, nv) -> {
                Platform.runLater(controller::resize);
        };
        scene.widthProperty().addListener(cl);
        scene.heightProperty().addListener(cl);                    

        // With the perspective camera it looks slightly different
//        PerspectiveCamera cam = new PerspectiveCamera(false);
//        cam.setFieldOfView(60);
//        cam.setTranslateX(0);
//        cam.setTranslateY(0);
//        cam.setTranslateZ(0);
//        scene.setCamera(cam);
        
        stage.setTitle("JavaFX MineSweeper");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
