package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;

public interface IModel {
    void generateMaze (int rows, int columns);
    int[][] getMaze ();
    Solution getSolution();
    int getCharacterPositionRow();
    int getCharacterPositionColumn();
    void moveCharacter (KeyCode movement);
    void solve ();
    void solutionOn2DArr();
    void save(String pathFile);
    void load(String pathFile);
    void stopServers();
    void changeConfiguration(String prop, String value);
    String getConfiguration (String prop);
    void exit();
}
