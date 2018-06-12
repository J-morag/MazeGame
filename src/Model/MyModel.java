package Model;

import Server.Server;
import javafx.scene.input.KeyCode;

import java.util.Observable;

public class MyModel  extends Observable implements IModel {

    Server server;

    public MyModel() {
    }

    @Override
    public void generateMaze(int rows, int columns) {

    }

    @Override
    public int[][] getMaze() {
        return new int[0][];
    }

    @Override
    public int getCharacterPositionRow() {
        return 0;
    }

    @Override
    public int getCharacterPositionColumn() {
        return 0;
    }

    @Override
    public void moveCharacter(KeyCode movement) {

    }
}
