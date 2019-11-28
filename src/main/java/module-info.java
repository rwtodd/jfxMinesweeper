module org.rwtodd.minesweeper {
   requires javafx.fxml;
   requires javafx.controls;
   exports org.rwtodd.minesweeper to javafx.graphics;
   opens org.rwtodd.minesweeper to javafx.fxml;
   opens org.rwtodd.minesweeper.fxml to javafx.fxml;
}
