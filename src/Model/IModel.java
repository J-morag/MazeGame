package Model;

import algorithms.mazeGenerators.Maze;
import javafx.scene.input.KeyCode;

public interface IModel {
    void generateMaze (int rows, int columns);
    int[][] getMaze ();
    int getCharacterPositionRow();
    int getCharacterPositionColumn();
    void moveCharacter (KeyCode movement);
    void solve ();
    void stopServers();
    void changeConfiguration(String prop, String value);
    void exit();
}
