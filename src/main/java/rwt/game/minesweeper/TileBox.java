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
import javafx.scene.shape.Path;

/**
 *
 * @author richa
 */
public final class TileBox extends StackPane {
    
    public TileBox(double width, double height, int n) {
        super();
   
        flipped = false;

        Box b = new Box(width, height, Math.min(width,height)*.3);
        b.setTranslateX(0);
        b.setTranslateY(0);
        b.setTranslateZ(0);
        PhongMaterial mat = new PhongMaterial(Color.LIGHTYELLOW);
        b.setMaterial(mat);
        getChildren().add(b);
        
        Box b2 = new Box(width*0.8, height*0.8, Math.min(width,height)*.1);
        mat = new PhongMaterial(Color.WHITESMOKE);
        b2.setMaterial(mat);
        b2.setTranslateX(0);
        b2.setTranslateY(0);
        b2.setTranslateZ(-b.getDepth()*0.5);
        getChildren().add(b2);
        
        if (n > 0) {
            Text t = new Text(0, 0, Integer.toString(n));
            Font f = Font.font(height);
            t.setFont(f);
//            while(t.getLayoutBounds().getHeight() >= height) {
//                f = Font.font(f.getSize()-1);
//                t.setFont(f);
//            }
            t.setFill(Color.BLUE);
            t.setFont(Font.font(Math.min(width,height)/3));
            t.setRotationAxis(javafx.scene.transform.Rotate.X_AXIS);
            t.setRotate(180);
            t.setTranslateZ(b.getDepth() * 0.50);
            getChildren().add(t);
        } else {
        }
    }
    
    private boolean flipped;
    
    public boolean hasFlipped() { return flipped; }
    
    public javafx.animation.Transition flip(Duration delay) {
        flipped = true;
       
        RotateTransition rt = new RotateTransition(Duration.seconds(0.25),this);
        rt.setAxis(javafx.scene.transform.Rotate.X_AXIS);
        rt.setFromAngle(0);
        rt.setToAngle(180);
        rt.setCycleCount(1);
        rt.setDelay(delay);    
        return rt;
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
