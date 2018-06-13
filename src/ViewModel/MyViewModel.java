package ViewModel;

import Model.IModel;
import Model.MyModel;
import algorithms.mazeGenerators.MyMazeGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;

import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer{

    private IModel model;

    private int characterPositionRowIndex;
    private int characterPositionColumnIndex;

    public StringProperty characterPositionRow = new SimpleStringProperty("0"); //For Binding
    public StringProperty characterPositionColumn = new SimpleStringProperty("0"); //For Binding
//    public StringProperty mazeGenerationAlgorithmConfig = new SimpleStringProperty("MyMazeGenerator");

    public enum EventType{
        MAZE, SOLUTION, MOVEMENT, INVALIDMOVEMENT, ERRORMESSAGE, MESSAGE
    }


    public MyViewModel(IModel model){
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model && arg == EventType.MAZE){
            characterPositionRowIndex = model.getCharacterPositionRow();
            characterPositionRow.set(characterPositionRowIndex + "");
            characterPositionColumnIndex = model.getCharacterPositionColumn();
            characterPositionColumn.set(characterPositionColumnIndex + "");
            setChanged();
            notifyObservers();
        }
    }

    public void generateMaze(int width, int height){
        model.generateMaze(width, height);
    }

    public void moveCharacter(KeyCode movement){
        model.moveCharacter(movement);
    }

    public int[][] getMaze() {
        return model.getMaze();
    }

    public int getCharacterPositionRow() {
        return characterPositionRowIndex;
    }

    public int getCharacterPositionColumn() {
        return characterPositionColumnIndex;
    }

    public static String getConfiguration(String prop) {
        return IModel.getConfiguration(prop);
    }
    public void changeConfiguration(String prop, String value) {
        model.changeConfiguration(prop, value);
        model.storeConfigurations();
    }

    public void exit(){
        model.stopServers();
    }
}
