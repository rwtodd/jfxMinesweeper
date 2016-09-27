/*
 * Copyright Richard Todd. I put the code under the
 * GPL v2.0.  See the LICENSE file in the repository.
 * for more information.
 */
package rwt.game.minesweeper;

import java.util.Random;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.geometry.Point3D;

/**
 *
 * @author richa
 */
public final class TileBox extends StackPane {
    
    // we'll have to store handles for our subcomponents
    // to make it easy to update them quickly.
    // The original design fetched them from getChildren() but
    // just seemed like too much runtime work to avoid storing a few
    // object handles.
    private Box outer, inner;
    private Text label;
    
    // Size the sub-components to target a given width and height.
    // Choose a font size that fits in the height, by guess-and-check
    public void setNewSize(double width, double height) {
        outer.setWidth(width);
        outer.setHeight(height);
        outer.setDepth(Math.min(width, height) * 0.3);
        
        inner.setWidth(width * 0.8);
        inner.setHeight(height * 0.8);
        inner.setDepth(Math.min(width, height) * 0.1);
        inner.setTranslateZ(-outer.getDepth()*0.5);
        
        if(label == null) return; // some TileBoxes don't have labels.
        
        Font f = Font.font(Math.max(height,width) + 4);
        label.setFont(f);

        while ( (label.getBoundsInLocal().getHeight() >= height) || 
                (label.getBoundsInLocal().getWidth() >= width) ) {
            f = Font.font(f.getSize() - 2);
            label.setFont(f);
        }
       
        label.setTranslateZ(outer.getDepth()*0.5);
    }
    
    public TileBox(double width, double height, int n) {
        super();
   
        flipped = false;
        flagged = false;
        
        outer = new Box();
        PhongMaterial mat = new PhongMaterial(Color.LIGHTYELLOW);
        outer.setMaterial(mat);
        getChildren().add(outer);
        
        inner = new Box();
        mat = new PhongMaterial(Color.WHITESMOKE);
        inner.setMaterial(mat);
        getChildren().add(inner);
        
        if (n > 0) {
            label = new Text(0, 0, Integer.toString(n));
            label.setFill(Color.BLUE);
            label.setRotationAxis(javafx.scene.transform.Rotate.X_AXIS);
            label.setRotate(180);
            getChildren().add(label);
        } else {
            label = null;
        }
        setNewSize(width, height);
    }
    
    private boolean flipped;
    private boolean flagged;
    
    public boolean hasFlipped() { return flipped; }
    public boolean isFlagged() { return flagged; }
    
    public javafx.animation.Transition flip(Duration delay) {
        flipped = true;
       
        RotateTransition rt = new RotateTransition(Duration.seconds(0.33),this);
        rt.setAxis(javafx.scene.transform.Rotate.X_AXIS);
        rt.setFromAngle(0);
        rt.setToAngle(180);
        rt.setCycleCount(1);
        rt.setDelay(delay);    
        return rt;
    }
    
    public void flag() {
        inner.setMaterial(new PhongMaterial(flagged?Color.WHITESMOKE:Color.AQUAMARINE));        
        flagged = !flagged;  // set/unset the flag.
    }
    
    Animation explode(final int diffx, final int diffy) {
         flipped = true;
         Random r = new Random();
         Duration p1s = Duration.seconds(0.1);
         Timeline explosion = new Timeline();
         explosion.setCycleCount(1);
         
         // calculate strength as inverse of distance
         double dist = Math.sqrt(diffx*diffx+diffy*diffy);
         double strength = (dist < 0.1) ? 2.0 : (1.0/dist);

         // change the rotation axis to something crazy..
         Point3D ra = getRotationAxis();         
         KeyFrame kf0 = new KeyFrame(Duration.ZERO, new KeyValue(rotationAxisProperty(), ra));
         KeyFrame kf1 = new KeyFrame(p1s, new KeyValue(rotationAxisProperty(), 
                                                       new Point3D((r.nextDouble()-0.5)*strength + ra.getX(),
                                                                   (r.nextDouble()-0.5)*strength + ra.getY(),
                                                                   (r.nextDouble()-0.5)*strength + ra.getZ())));
         explosion.getKeyFrames().addAll(kf0,kf1);

         // now rotate it a bit...         
         kf0 = new KeyFrame(Duration.ZERO, new KeyValue(rotateProperty(), getRotate()));
         kf1 = new KeyFrame(p1s, new KeyValue(rotateProperty(), 
                                              getRotate() + (r.nextDouble()-0.5)*90*strength));
         explosion.getKeyFrames().addAll(kf0,kf1);

         // now move the whole tile slightly...
         double endX = r.nextDouble()*2.0*strength*getWidth()*Math.signum(diffx);
         double endY = r.nextDouble()*2.0*strength*getHeight()*Math.signum(diffy);

         kf0 = new KeyFrame(Duration.ZERO, new KeyValue(layoutXProperty(), getLayoutX()));
         kf1 = new KeyFrame(p1s,new KeyValue(layoutXProperty(), getLayoutX() + endX));
         explosion.getKeyFrames().addAll(kf0,kf1);
         
         kf0 = new KeyFrame(Duration.ZERO, new KeyValue(layoutYProperty(), getLayoutY()));
         kf1 = new KeyFrame(p1s,new KeyValue(layoutYProperty(), getLayoutY() + endY));
         explosion.getKeyFrames().addAll(kf0,kf1);
         
         // start a little later if we are far from the blast site
         explosion.setDelay(Duration.seconds(dist * 0.02));
         return explosion;
    }
}
