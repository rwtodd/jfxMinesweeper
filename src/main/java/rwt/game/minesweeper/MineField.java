/*
 * Copyright Richard Todd. I put the code under the
 * GPL v2.0.  See the LICENSE file in the repository.
 * for more information.
 */
package rwt.game.minesweeper;

import java.util.Random;

/**
 *
 * @author richa
 */
public class MineField {
   private final byte[][] field;
   
   public MineField(final int w, final int h, double pct) {
       field = new byte[w][h];
       Random rnd = new Random();
       
       // first place the bombs... (-1 == BOMB!)
       for(int y = 0; y < h; ++y) {
           for(int x = 0; x < w; ++x) {
               field[x][y] = (byte)(( rnd.nextFloat() < pct ) ? -1 : 0);
           }
       }
       for(int y = 0; y < h; ++y) {
           for(int x = 0; x < w; ++x) {
               field[x][y] = (byte)(slowNeighborCount(x, y));
           }
       }
       
   }
   
   public boolean hasBomb(final int x, final int y) {
        return field[x][y] == ((byte)-1);    
   }
   
   public int countNeighbors(final int x, final int y) {
       if(hasBomb(x,y)) return 0;
       return ((int)field[x][y]);
   }
   
   private int slowNeighborCount(int x, int y) {
       if(hasBomb(x,y)) return -1;
       
       int count = 0;
       int minx = Math.max(x-1,0);
       int maxx = Math.min(x+1,field.length-1);
       int miny = Math.max(y-1,0);
       int maxy = Math.min(y+1,field[0].length-1);
       for (y = miny; y <= maxy; ++y) {
           for (x = minx; x <= maxx; ++x) {
               count += hasBomb(x, y) ? 1 : 0;
           }
       }
       return count;
   }
           
}
