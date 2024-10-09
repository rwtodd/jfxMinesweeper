module rwt.minesweeper {
   requires javafx.fxml;
   requires javafx.controls;
   requires transitive javafx.graphics;
   exports rwt.minesweeper to javafx.graphics;
   opens rwt.minesweeper to javafx.fxml;
}
