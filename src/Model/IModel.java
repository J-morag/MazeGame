package Model;

import javafx.scene.input.KeyCode;

public interface IModel {
    void generateMaze (int rows, int columns);
    int[][] getMaze ();
    int getCharacterPositionRow();
    int getCharacterPositionColumn();
    void moveCharacter (KeyCode movement);
}
